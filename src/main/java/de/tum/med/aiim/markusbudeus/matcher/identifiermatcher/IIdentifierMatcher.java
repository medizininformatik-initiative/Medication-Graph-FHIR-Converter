package de.tum.med.aiim.markusbudeus.matcher.identifiermatcher;

public interface IIdentifierMatcher<S> {

	/**
	 * Attempts to match the given identifier to known identifiers. Never returns null.
	 */
	Match<S> findMatch(S searchTerm);

	/**
	 * Attempts to match the given searchTerm to known identifiers. Never returns null.
	 */
	default Match<S> findMatchWithTransformation(String searchTerm) {
		return findMatch(transform(searchTerm));
	}

	S transform(String name);

}
