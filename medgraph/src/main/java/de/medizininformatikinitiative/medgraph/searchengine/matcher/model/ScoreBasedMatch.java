package de.medizininformatikinitiative.medgraph.searchengine.matcher.model;

import de.medizininformatikinitiative.medgraph.searchengine.provider.MappedIdentifier;

/**
 * A match which has a given score based on whatever metric the corresponding matcher uses when comparing it with the
 * given search term.
 *
 * @author Markus Budeus
 */
public class ScoreBasedMatch<S> extends Match<S> {
	private final double score;

	/**
	 * Creates a new distance based match.
	 *
	 * @param matchedIdentifier the matched identifier
	 * @param score             the score assigned to the identifier according to the matcher's metric
	 */
	public ScoreBasedMatch(MappedIdentifier<S> matchedIdentifier, double score) {
		super(matchedIdentifier);
		this.score = score;
	}

	/**
	 * Returns the distance between the identifier and the used search term.
	 */
	public double getScore() {
		return score;
	}
}
