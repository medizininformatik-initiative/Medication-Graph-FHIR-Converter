package de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement;

import de.medizininformatikinitiative.medgraph.searchengine.matcher.EditDistanceListMatcher;
import de.medizininformatikinitiative.medgraph.searchengine.matcher.editdistance.FlexibleLevenshteinDistanceService;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.EdqmConcept;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.EdqmPharmaceuticalDoseForm;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.Identifier;
import de.medizininformatikinitiative.medgraph.searchengine.provider.BaseProvider;
import de.medizininformatikinitiative.medgraph.searchengine.provider.IdentifierProvider;
import de.medizininformatikinitiative.medgraph.searchengine.stringtransformer.RemoveBlankStrings;
import de.medizininformatikinitiative.medgraph.searchengine.stringtransformer.ToLowerCase;
import de.medizininformatikinitiative.medgraph.searchengine.stringtransformer.TraceableTransformer;
import de.medizininformatikinitiative.medgraph.searchengine.stringtransformer.WhitespaceTokenizer;
import de.medizininformatikinitiative.medgraph.searchengine.tools.SearchEngineTools;
import de.medizininformatikinitiative.medgraph.searchengine.tools.SearchEngineTools.OverlapResolutionStrategy;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.DistinctMultiSubstringUsageStatement;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.StringListUsageStatement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Extracts dose form data from a human-written query string.
 *
 * @author Markus Budeus
 */
public class DoseFormQueryRefiner implements PartialQueryRefiner<DoseFormQueryRefiner.Result> {

	private final EditDistanceListMatcher editDistanceListMatcher = new EditDistanceListMatcher(
			// Maximum allowed edit distance is
			//   0 for up to 4 characters
			//   1 for 5-7 characters
			//   2 for 8-10 characters
			//   3 for 11 or more characters
			new FlexibleLevenshteinDistanceService(l -> Math.min(3, (l - 2) / 3))
	);
	private final TraceableTransformer<String, List<String>, DistinctMultiSubstringUsageStatement, StringListUsageStatement> transformer =
			new ToLowerCase()
					.andTraceable(new WhitespaceTokenizer(false))
					.andTraceable(new RemoveBlankStrings());
	private final IdentifierProvider<List<String>> edqmConceptsProvider;

	public DoseFormQueryRefiner(BaseProvider<String> edqmConceptsProvider) {
		this.edqmConceptsProvider = edqmConceptsProvider
				.parallel()
				.withTransformation(transformer);
	}

	/**
	 * Extracts dose form information from the given query identifier.
	 *
	 * @param query the query identifier from which to extract information
	 * @return a {@link Result}-object providing the detected dose forms and dose form characteristics as well as
	 * information on where in the query string they were found
	 */
	public Result parse(Identifier<String> query) {
		Identifier<List<String>> transformedQuery = transformer.apply(query);

		List<EditDistanceListMatcher.Match> matches = editDistanceListMatcher.match(transformedQuery, edqmConceptsProvider)
		                       .filter(match -> {
			                       if (match.getMatchedIdentifier().target instanceof EdqmConcept) {
				                       return true;
			                       }
			                       System.err.println(
					                       "Warning: The provider provided an Identifiable which is no EdqmConcept! (Got " + match.getMatchedIdentifier().target + ")");
			                       return false;
		                       }).collect(Collectors.toList());

		SearchEngineTools.removeConflictingOverlaps(matches, this::overlap, this::checkPriorityOnOverlap);

		List<EdqmPharmaceuticalDoseForm> doseForms = new ArrayList<>();
		List<EdqmConcept> characteristics = new ArrayList<>();
		Set<Integer> usedTokens = new HashSet<>();

		matches.forEach(match -> {
			EdqmConcept concept = (EdqmConcept) match.getMatchedIdentifier().target;
			if (concept instanceof EdqmPharmaceuticalDoseForm df) {
				doseForms.add(df);
			} else {
				characteristics.add(concept);
			}
			usedTokens.addAll(match.getUsageStatement().getUsedIndices());
		});

		// Construct a new StringListUsageStatement which covers all used tokens of the search term
		StringListUsageStatement completeUsageStatement = new StringListUsageStatement(transformedQuery.getIdentifier(),
				usedTokens);
		// Then reverse this usage statement back to the original query
		DistinctMultiSubstringUsageStatement usageStatement = transformer.reverseTransformUsageStatement(query,
				completeUsageStatement);

		return new Result(doseForms, characteristics, usageStatement);
	}

	/**
	 * Returns how to handle conflicts between two matches.
	 */
	private OverlapResolutionStrategy checkPriorityOnOverlap(EditDistanceListMatcher.Match match1, EditDistanceListMatcher.Match match2) {
		int editDistance1 = match1.getDistance().editDistance();
		int editDistance2 = match2.getDistance().editDistance();

		if (editDistance1 > editDistance2) return OverlapResolutionStrategy.KEEP_SECOND;
		else if (editDistance2 > editDistance1) return OverlapResolutionStrategy.KEEP_FIRST;
		return OverlapResolutionStrategy.KEEP_BOTH;
	}

	/**
	 * Returns whether the source tokens from the two given matches overlap.
	 */
	private boolean overlap(EditDistanceListMatcher.Match match1, EditDistanceListMatcher.Match match2) {
		Set<Integer> set1 = match1.getUsageStatement().getUsedIndices();
		Set<Integer> set2 = match2.getUsageStatement().getUsedIndices();
		HashSet<Integer> union = new HashSet<>(set1);
		union.addAll(set2);
		return union.size() < set1.size() + set2.size();
	}

	public static class Result implements PartialQueryRefiner.Result {
		private final List<EdqmPharmaceuticalDoseForm> doseForms;
		private final List<EdqmConcept> characteristics;
		private final DistinctMultiSubstringUsageStatement usageStatement;

		private Result(List<EdqmPharmaceuticalDoseForm> doseForms, List<EdqmConcept> characteristics,
		               DistinctMultiSubstringUsageStatement usageStatement) {
			this.doseForms = doseForms;
			this.characteristics = characteristics;
			this.usageStatement = usageStatement;
		}

		@Override
		public DistinctMultiSubstringUsageStatement getUsageStatement() {
			return usageStatement;
		}

		public List<EdqmPharmaceuticalDoseForm> getDoseForms() {
			return doseForms;
		}

		public List<EdqmConcept> getCharacteristics() {
			return characteristics;
		}

		@Override
		public void incrementallyApply(SearchQuery.Builder searchQueryBuilder) {
			searchQueryBuilder.withDoseForms(doseForms)
			                  .withDoseFormCharacteristics(characteristics);
		}
	}

}
