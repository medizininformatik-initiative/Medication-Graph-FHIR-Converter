package de.medizininformatikinitiative.medgraph.searchengine.matcher;

import de.medizininformatikinitiative.medgraph.searchengine.matcher.model.DistanceBasedMatch;
import de.medizininformatikinitiative.medgraph.searchengine.matcher.model.ScoreBasedMatch;
import de.medizininformatikinitiative.medgraph.searchengine.provider.MappedIdentifier;

/**
 * Superclass used by matchers which rank results by calculating a score for each possible match based on the search
 * term.
 *
 * @param <S> the type of search term and target objects used
 * @author Markus Budeus
 */
public abstract class ScoreBasedMatcher<S> extends SimpleMatcher<S, ScoreBasedMatch<S>> {

	@Override
	protected ScoreBasedMatch<S> match(S searchTerm, MappedIdentifier<S> mi) {
		double score = calculateScore(searchTerm, mi.identifier.getIdentifier());
		if (score > 0) return new ScoreBasedMatch<>(mi, score);
		return null;
	}

	/**
	 * Calculates the score for the given target based on the search term. A score must be strictly greater than zero
	 * for the target to be considered a match.
	 */
	public abstract double calculateScore(S searchTerm, S target);

}
