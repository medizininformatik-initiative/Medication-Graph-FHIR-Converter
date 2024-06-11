package de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject;

import de.medizininformatikinitiative.medgraph.searchengine.matcher.IMatcher;
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

	/**
	 * The matcher which produced the corresponding match.
	 */
	private final IMatcher<?, ?, T> matcher;

	public MatchOrigin(T match, IMatcher<?, ?, T> matcher) {
		this.match = match;
		this.matcher = matcher;
	}

	public T getMatch() {
		return match;
	}

	public IMatcher<?, ?, T> getMatcher() {
		return matcher;
	}
}
