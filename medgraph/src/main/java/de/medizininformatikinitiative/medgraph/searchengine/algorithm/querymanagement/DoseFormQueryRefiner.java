package de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement;

import de.medizininformatikinitiative.medgraph.searchengine.matcher.LevenshteinListMatcher;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.EdqmConcept;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.EdqmPharmaceuticalDoseForm;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Identifiable;
import de.medizininformatikinitiative.medgraph.searchengine.provider.BaseProvider;
import de.medizininformatikinitiative.medgraph.searchengine.provider.IdentifierProvider;
import de.medizininformatikinitiative.medgraph.searchengine.stringtransformer.ToLowerCase;
import de.medizininformatikinitiative.medgraph.searchengine.stringtransformer.TraceableTransformer;
import de.medizininformatikinitiative.medgraph.searchengine.stringtransformer.WhitespaceTokenizer;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.*;

import java.util.*;

/**
 * Extracts dose form data from a human-written query string.
 *
 * @author Markus Budeus
 */
public class DoseFormQueryRefiner implements PartialQueryRefiner<DoseFormQueryRefiner.Result> {

	private final LevenshteinListMatcher levenshteinListMatcher = new LevenshteinListMatcher(1);
	private final TraceableTransformer<String, List<String>, DistinctMultiSubstringUsageStatement, StringListUsageStatement> transformer =
			new ToLowerCase()
					.andTraceable(new WhitespaceTokenizer(false));
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
		List<LevenshteinListMatcher.Match> matches = new ArrayList<>();

		levenshteinListMatcher.match(transformer.apply(query), edqmConceptsProvider)
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

//		removeOverlaps(matches);

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

//	/**
//	 * Removes matches from the given list if they overlap with each other. The match which consists of more tokens from
//	 * the identifier survives. If two overlapping matches have the same amount of identifier tokens, the first one in
//	 * the list survives.
//	 */
//	private void removeOverlaps(List<LevenshteinListMatcher.Match> matches) {
//		for (int i = matches.size() - 1; i > 0; i--) {
//			LevenshteinListMatcher.Match current = matches.get(i);
//			for (int j = i - 1; j >= 0; j--) {
//				LevenshteinListMatcher.Match opponent = matches.get(j);
//				if (overlap(current, opponent)) {
//					// Well, one of you has to die.
//					if (firstHasPriority(opponent, current)) {
//						matches.remove(i);
//						break;
//					} else {
//						matches.remove(j);
//						i--;
//					}
//				}
//			}
//		}
//	}
//
//	/**
//	 * Assuming one of the given matches has to be eliminated from the result, determines if the first one
//	 * shall be the one to survive. Instances of {@link EdqmPharmaceuticalDoseForm} have priority over simple
//	 * {@link EdqmConcept}-instances. If this does not decide the match, whoever uses more tokens has priority.
//	 * If then it is still a draw, the first match gets priority.
//	 */
//	private boolean firstHasPriority(LevenshteinListMatcher.Match match1, LevenshteinListMatcher.Match match2) {
//		if ((match1.getMatchedIdentifier().target instanceof EdqmPharmaceuticalDoseForm)
//				&& !(match2.getMatchedIdentifier().target instanceof EdqmPharmaceuticalDoseForm)) {
//			return true;
//		} else if (!(match1.getMatchedIdentifier().target instanceof EdqmPharmaceuticalDoseForm)
//				&& (match2.getMatchedIdentifier().target instanceof EdqmPharmaceuticalDoseForm)) {
//			return false;
//		} else {
//			if (match1.getUsageStatement().getUsedIndices().size() >=
//					match2.getUsageStatement().getUsedIndices().size()) {
//				return true;
//			} else {
//				return false;
//			}
//		}
//
//	}
//
//	/**
//	 * Returns whether the source tokens from the two given matches overlap.
//	 */
//	private boolean overlap(LevenshteinListMatcher.Match match1, LevenshteinListMatcher.Match match2) {
//		Set<Integer> set1 = match1.getUsageStatement().getUsedIndices();
//		Set<Integer> set2 = match2.getUsageStatement().getUsedIndices();
//		HashSet<Integer> union = new HashSet<>(set1);
//		union.addAll(set2);
//		return union.size() < set1.size() + set2.size();
//	}

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
