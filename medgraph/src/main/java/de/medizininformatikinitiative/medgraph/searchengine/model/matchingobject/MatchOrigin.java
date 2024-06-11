package de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject;

import de.medizininformatikinitiative.medgraph.searchengine.matcher.model.Match;

/**
 * Indicates an original match originated from a matcher output.
 *
 * @author Markus Budeus
 */
public class MatchOrigin<T extends Match<?, ?>> implements Origin {

	/**
	 * The match which is the match source represented by this instance.
	 */
	private final T match;

	public MatchOrigin(T match) {
		this.match = match;
	}

	public T getMatch() {
		return match;
	}

}
