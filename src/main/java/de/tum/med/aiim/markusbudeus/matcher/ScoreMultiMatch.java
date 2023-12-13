package de.tum.med.aiim.markusbudeus.matcher;

import de.tum.med.aiim.markusbudeus.matcher.provider.Identifier;

import java.util.*;

public class ScoreMultiMatch<S> implements Match<S> {

	/**
	 * The matches, ordered by their score in descending order.
	 */
	public final List<MatchWithScore<S>> matchesWithScore;

	public ScoreMultiMatch(Map<Identifier<S>, Double> matchesWithScore) {
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
	public Set<Identifier<S>> getBestMatches() {
		Set<Identifier<S>> bestMatches = new HashSet<>();
		if (matchesWithScore.isEmpty()) return bestMatches;
		double topScore = matchesWithScore.get(0).score;
		for (MatchWithScore<S> match : matchesWithScore) {
			if (match.score < topScore) break;
			bestMatches.add(match.match);
		}
		return bestMatches;
	}

	public static class MatchWithScore<S> {
		final Identifier<S> match;
		final double score;

		public MatchWithScore(Identifier<S> match, double score) {
			this.match = match;
			this.score = score;
		}
	}

}
