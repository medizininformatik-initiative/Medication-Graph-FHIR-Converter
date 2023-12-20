package de.tum.med.aiim.markusbudeus.matcher.identifiermatcher;

public interface IIdentifierMatcher<S> {

	/**
	 * Attempts to match the given identifier to known identifiers. Never returns null.
	 */
	Match<S> findMatchWithoutTransformation(S searchTerm);

	/**
	 * Attempts to match the given searchTerm to known identifiers. Never returns null.
	 */
	default Match<S> findMatch(String searchTerm) {
		return findMatchWithoutTransformation(transform(searchTerm));
	}

	S transform(String name);

}
