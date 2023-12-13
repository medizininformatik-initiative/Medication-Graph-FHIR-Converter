package de.tum.med.aiim.markusbudeus.matcher.stringmatcher;

import de.tum.med.aiim.markusbudeus.matcher.Match;

public interface IStringMatcher<S> {

	/**
	 * Attempts to match the given identifier to known identifiers. Never returns null.
	 */
	Match<S> findMatch(S name);

	/**
	 * Attempts to match the given name to known identifiers. Never returns null.
	 */
	default Match<S> findMatchWithTransformation(String name) {
		return findMatch(transform(name));
	}

	S transform(String name);

}
