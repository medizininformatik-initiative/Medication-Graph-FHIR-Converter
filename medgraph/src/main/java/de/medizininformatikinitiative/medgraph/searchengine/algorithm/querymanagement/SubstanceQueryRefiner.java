package de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement;

import de.medizininformatikinitiative.medgraph.searchengine.matcher.EditDistanceSetMatcher;
import de.medizininformatikinitiative.medgraph.searchengine.matcher.editdistance.FlexibleLevenshteinDistanceService;
import de.medizininformatikinitiative.medgraph.searchengine.matcher.editdistance.LevenshteinDistanceService;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Identifiable;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Substance;
import de.medizininformatikinitiative.medgraph.searchengine.provider.IdentifierStream;
import de.medizininformatikinitiative.medgraph.searchengine.stringtransformer.*;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.DistinctMultiSubstringUsageStatement;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.StringSetUsageStatement;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.SubstringUsageStatement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Partial query refiner which resolves substances from the search term.
 *
 * @author Markus Budeus
 */
public class SubstanceQueryRefiner implements PartialQueryRefiner<SubstanceQueryRefiner.Result> {

	private final TraceableTransformer<String, Set<String>, DistinctMultiSubstringUsageStatement, StringSetUsageStatement> TRANSFORMER =
			new ToLowerCase().
					andTraceable(new WhitespaceTokenizer())
					.andTraceable(new TrimSpecialSuffixSymbols())
					.andTraceable(new RemoveBlankStrings())
					.andTraceable(new MinimumTokenLength(2))
					.andTraceable(new ListToSet());

	private final EditDistanceSetMatcher editDistanceSetMatcher = new EditDistanceSetMatcher(
			// Maximum allowed edit distance is
			//   0 for up to 3 characters
			//   1 for 4-6 characters
			//   2 for 7-9 characters
			//   3 for 10 or more characters
			new FlexibleLevenshteinDistanceService(l -> Math.min(3, (l-1) / 3))
	);

	private final IdentifierStream<String> substanceProvider;

	public SubstanceQueryRefiner(IdentifierStream<String> substanceProvider) {
		this.substanceProvider = substanceProvider.parallel();
	}

	@Override
	public Result parse(String query) {
		List<Substance> substances = new ArrayList<>();
		Set<String> usedTokens = new HashSet<>();
		editDistanceSetMatcher
				.match(TRANSFORMER.apply(query), substanceProvider.withTransformation(TRANSFORMER))
				.filter(match -> {
					Identifiable target = match.getMatchedIdentifier().target;
					if (target instanceof Substance) {
						return true;
					}
					System.err.println("Warning: The provider passed to the SubstanceQueryRefiner provided an " +
							"object which is not a substance: " + target);
					return false;
				})
				.forEach(match -> {
					synchronized (this) {
						substances.add((Substance) match.getMatchedIdentifier().target);
						usedTokens.addAll(match.getUsageStatement().getUsedTokens());
					}
				});

		DistinctMultiSubstringUsageStatement usageStatement =
				TRANSFORMER.reverseTransformUsageStatement(query,
						new StringSetUsageStatement(TRANSFORMER.apply(query), usedTokens));

		return new Result(substances, usageStatement);
	}

	public static class Result implements PartialQueryRefiner.Result {

		private final List<Substance> substances;
		private final DistinctMultiSubstringUsageStatement usageStatement;

		public Result(List<Substance> substances, DistinctMultiSubstringUsageStatement usageStatement) {
			this.substances = substances;
			this.usageStatement = usageStatement;
		}

		@Override
		public void incrementallyApply(SearchQuery.Builder searchQueryBuilder) {
			searchQueryBuilder.withSubstances(substances);
		}

		@Override
		public SubstringUsageStatement getUsageStatement() {
			return usageStatement;
		}

		public List<Substance> getSubstances() {
			return substances;
		}
	}

}
