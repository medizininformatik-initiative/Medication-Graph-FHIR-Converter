package de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement;

import de.medizininformatikinitiative.medgraph.searchengine.matcher.EditDistanceSetMatcher;
import de.medizininformatikinitiative.medgraph.searchengine.matcher.editdistance.FlexibleLevenshteinDistanceService;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Identifiable;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Substance;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.Identifier;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchOrigin;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Origin;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.OriginalMatch;
import de.medizininformatikinitiative.medgraph.searchengine.provider.IdentifierStream;
import de.medizininformatikinitiative.medgraph.searchengine.stringtransformer.*;
import de.medizininformatikinitiative.medgraph.searchengine.tools.SearchEngineTools;
import de.medizininformatikinitiative.medgraph.searchengine.tools.SearchEngineTools.OverlapResolutionStrategy;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.DistinctMultiSubstringUsageStatement;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.StringSetUsageStatement;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.SubstringUsageStatement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Partial query refiner which resolves substances from the search term.
 *
 * @author Markus Budeus
 */
public class NewSubstanceQueryRefiner implements NewPartialQueryRefiner<NewSubstanceQueryRefiner.Result> {

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
			new FlexibleLevenshteinDistanceService(l -> Math.min(3, (l - 1) / 3))
	);

	private final IdentifierStream<String> substanceProvider;

	public NewSubstanceQueryRefiner(IdentifierStream<String> substanceProvider) {
		this.substanceProvider = substanceProvider.parallel();
	}

	@Override
	public Result parse(Identifier<String> query) {
		List<MatchingObject<Substance>> substances = new ArrayList<>();
		Set<String> usedTokens = new HashSet<>();
		Identifier<Set<String>> transformedQuery = TRANSFORMER.apply(query);
		List<EditDistanceSetMatcher.Match> matches = editDistanceSetMatcher
				.match(transformedQuery, substanceProvider.withTransformation(TRANSFORMER))
				.filter(match -> {
					Identifiable target = match.getMatchedIdentifier().target;
					if (target instanceof Substance) {
						return true;
					}
					System.err.println("Warning: The provider passed to the SubstanceQueryRefiner provided an " +
							"object which is not a substance: " + target);
					return false;
				}).collect(Collectors.toList());

		SearchEngineTools.removeConflictingOverlaps(matches, this::overlap, this::checkPriorityOnOverlap);

		matches.forEach(match -> {
			Substance substance = (Substance) match.getMatchedIdentifier().target;
			Origin origin = new MatchOrigin<>(match, editDistanceSetMatcher);
			substances.add(new OriginalMatch<>(substance, origin));
			usedTokens.addAll(match.getUsageStatement().getUsedTokens());
		});

		DistinctMultiSubstringUsageStatement usageStatement =
				TRANSFORMER.reverseTransformUsageStatement(query,
						new StringSetUsageStatement(transformedQuery.getIdentifier(), usedTokens));

		return new Result(substances, usageStatement);
	}

	/**
	 * Returns how to handle conflicts between two matches.
	 */
	private OverlapResolutionStrategy checkPriorityOnOverlap(EditDistanceSetMatcher.Match match1, EditDistanceSetMatcher.Match match2) {
		double score1 = match1.getScore();
		double score2 = match2.getScore();

		if (score1 > score2) return OverlapResolutionStrategy.KEEP_FIRST;
		else if (score2 > score1) return OverlapResolutionStrategy.KEEP_SECOND;
		return OverlapResolutionStrategy.KEEP_BOTH;
	}

	/**
	 * Returns whether the source tokens from the two given matches overlap.
	 */
	private boolean overlap(EditDistanceSetMatcher.Match match1, EditDistanceSetMatcher.Match match2) {
		Set<String> set1 = match1.getUsageStatement().getUsedParts();
		Set<String> set2 = match2.getUsageStatement().getUsedParts();
		HashSet<String> union = new HashSet<>(set1);
		union.addAll(set2);
		return union.size() < set1.size() + set2.size();
	}

	public static class Result implements NewPartialQueryRefiner.Result {

		private final List<MatchingObject<Substance>> substances;
		private final DistinctMultiSubstringUsageStatement usageStatement;

		public Result(List<MatchingObject<Substance>> substances, DistinctMultiSubstringUsageStatement usageStatement) {
			this.substances = substances;
			this.usageStatement = usageStatement;
		}

		@Override
		public SubstringUsageStatement getUsageStatement() {
			return usageStatement;
		}

		public List<MatchingObject<Substance>> getSubstances() {
			return substances;
		}

		@Override
		public void incrementallyApply(NewRefinedQuery.Builder refinedQueryBuilder) {
			substances.forEach(refinedQueryBuilder::withSubstance);
		}
	}

}
