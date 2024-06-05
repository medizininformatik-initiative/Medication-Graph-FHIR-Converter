package de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement;

import de.medizininformatikinitiative.medgraph.searchengine.matcher.LevenshteinSetMatcher;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Identifiable;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Substance;
import de.medizininformatikinitiative.medgraph.searchengine.provider.IdentifierStream;
import de.medizininformatikinitiative.medgraph.searchengine.stringtransformer.*;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.DistinctMultiSubstringUsageStatement;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.StringSetUsageStatement;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.SubstringUsageStatement;

import java.util.*;

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
//					.andTraceable(new RemoveBlankStrings()); TODO
					.andTraceable(new ListToSet());

	private final LevenshteinSetMatcher levenshteinSetMatcher = new LevenshteinSetMatcher();

	private final IdentifierStream<String> substanceProvider;

	public SubstanceQueryRefiner(IdentifierStream<String> substanceProvider) {
		this.substanceProvider = substanceProvider.parallel();
	}

	@Override
	public Result parse(String query) {
		List<Substance> substances = new ArrayList<>();
		Set<String> usedTokens = new HashSet<>();
		levenshteinSetMatcher
				.match(TRANSFORMER.apply(query), substanceProvider.withTransformation(TRANSFORMER))
				.filter(Objects::nonNull)
				.forEach(match -> {
					Identifiable target = match.getMatchedIdentifier().target;
					if (target instanceof Substance s) {
						substances.add(s);
						usedTokens.addAll(match.getUsageStatement().getUsedTokens());
					} else {
						System.err.println("Warning: The provider passed to the SubstanceQueryRefiner provided an " +
								"object which is not a substance: " + target);
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
