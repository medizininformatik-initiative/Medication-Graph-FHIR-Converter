package de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement;

import de.medizininformatikinitiative.medgraph.searchengine.matcher.EditDistanceListMatcher;
import de.medizininformatikinitiative.medgraph.searchengine.matcher.editdistance.LevenshteinDistanceService;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.EdqmConcept;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.EdqmPharmaceuticalDoseForm;
import de.medizininformatikinitiative.medgraph.searchengine.provider.BaseProvider;
import de.medizininformatikinitiative.medgraph.searchengine.provider.IdentifierProvider;
import de.medizininformatikinitiative.medgraph.searchengine.stringtransformer.RemoveBlankStrings;
import de.medizininformatikinitiative.medgraph.searchengine.stringtransformer.ToLowerCase;
import de.medizininformatikinitiative.medgraph.searchengine.stringtransformer.TraceableTransformer;
import de.medizininformatikinitiative.medgraph.searchengine.stringtransformer.WhitespaceTokenizer;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.DistinctMultiSubstringUsageStatement;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.StringListUsageStatement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Extracts dose form data from a human-written query string.
 *
 * @author Markus Budeus
 */
public class DoseFormQueryRefiner implements PartialQueryRefiner<DoseFormQueryRefiner.Result> {

	private static final int EQUAL_PRIORITY = 0;
	private static final int FIRST_HAS_PRIORITY = 1;
	private static final int SECOND_HAS_PRIORITY = 2;

	private final EditDistanceListMatcher editDistanceListMatcher = new EditDistanceListMatcher(new LevenshteinDistanceService(1));
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
	 * Extracts dose form information from the given query string.
	 *
	 * @param query the query string from which to extract information
	 * @return a {@link Result}-object providing the detected dose forms and dose form characteristics as well as
	 * information on where in the query string they were found
	 */
	public Result parse(String query) {
		List<EditDistanceListMatcher.Match> matches = new ArrayList<>();

		editDistanceListMatcher.match(transformer.apply(query), edqmConceptsProvider)
		                       .filter(match -> {
			                      if (match.getMatchedIdentifier().target instanceof EdqmConcept) {
				                      return true;
			                      }
			                      System.err.println(
					                      "Warning: The provider provided an Identifiable which is no EdqmConcept! (Got " + match.getMatchedIdentifier().target + ")");
			                      return false;
		                      }).forEach(m -> {
			                      synchronized (this) {
				                      matches.add(m);
			                      }
		                      });

		removeProblematicOverlaps(matches);

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
		StringListUsageStatement completeUsageStatement = new StringListUsageStatement(transformer.apply(query),
				usedTokens);
		// Then reverse this usage statement back to the original query
		DistinctMultiSubstringUsageStatement usageStatement = transformer.reverseTransformUsageStatement(query,
				completeUsageStatement);

		return new Result(doseForms, characteristics, usageStatement);
	}

	/**
	 * Removes matches from the given list if they overlap with each other and if one of the matches of each overlapping
	 * pair is considered to be less relevant.
	 */
	private void removeProblematicOverlaps(List<EditDistanceListMatcher.Match> matches) {
		for (int i = matches.size() - 1; i > 0; i--) {
			EditDistanceListMatcher.Match current = matches.get(i);
			for (int j = i - 1; j >= 0; j--) {
				EditDistanceListMatcher.Match opponent = matches.get(j);
				if (overlap(current, opponent)) {
					// Remove Overlap if required
					int overlapPriority = checkPriorityOnOverlap(opponent, current);
					if (overlapPriority == FIRST_HAS_PRIORITY) {
						matches.remove(i);
						break;
					} else if (overlapPriority == SECOND_HAS_PRIORITY) {
						matches.remove(j);
						i--;
					}
				}
			}
		}
	}

	/**
	 * Returns a code indicating which one of these matches is more relevant or if they are equally relevant, assuming
	 * they overlap in the source. The return value is either {@link #FIRST_HAS_PRIORITY}, {@link #SECOND_HAS_PRIORITY}
	 * or {@link #EQUAL_PRIORITY}
	 */
	private int checkPriorityOnOverlap(EditDistanceListMatcher.Match match1, EditDistanceListMatcher.Match match2) {
		int editDistance1 = match1.getDistance().getEditDistance();
		int editDistance2 = match2.getDistance().getEditDistance();

		if (editDistance1 > editDistance2) return SECOND_HAS_PRIORITY;
		else if (editDistance2 > editDistance1) return  FIRST_HAS_PRIORITY;
		return EQUAL_PRIORITY;
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
