package de.tum.med.aiim.markusbudeus.matcher2.matchers.model;

import de.tum.med.aiim.markusbudeus.matcher2.data.ScoreSortDirective;
import de.tum.med.aiim.markusbudeus.matcher2.provider.MappedIdentifier;

import java.util.*;

public class ScoreMultiMatch<S> implements Match<S> {

	public static <S> ScoreMultiMatch<S> scoreByDistance(Map<MappedIdentifier<S>, Integer> distances) {
		List<MatchWithScore<S>> list = new ArrayList<>(distances.size());
		distances.forEach((identifier, distance) -> list.add(new MatchWithScore<>(identifier, 1.0 / (distance + 1))));
		return new ScoreMultiMatch<>(list);
	}

	/**
	 * The matches, ordered by their score in descending order.
	 */
	public final List<MatchWithScore<S>> matchesWithScore;

	public ScoreMultiMatch(Map<MappedIdentifier<S>, Double> matchesWithScore) {
		this.matchesWithScore = new ArrayList<>();
		matchesWithScore.forEach((match, score) -> this.matchesWithScore.add(new MatchWithScore<S>(match, score)));
		sortMatches();
	}

	public ScoreMultiMatch(List<MatchWithScore<S>> matchesWithScore) {
		this.matchesWithScore = matchesWithScore;
		sortMatches();
	}

	private void sortMatches() {
		this.matchesWithScore.sort(Comparator.comparingDouble((MatchWithScore<S> e) -> -e.score));
	}

	@Override
	public Set<MappedIdentifier<S>> getBestMatches() {
		Set<MappedIdentifier<S>> bestMatches = new HashSet<>();
		if (matchesWithScore.isEmpty()) return bestMatches;
		double topScore = matchesWithScore.get(0).score;
		for (MatchWithScore<S> match : matchesWithScore) {
			if (match.score < topScore) break;
			bestMatches.add(match.match);
		}
		return bestMatches;
	}

	public static class MatchWithScore<S> {
		public final MappedIdentifier<S> match;
		public final double score;

		public MatchWithScore(MappedIdentifier<S> match, double score) {
			this.match = match;
			this.score = score;
		}
	}

}
