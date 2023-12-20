package de.tum.med.aiim.markusbudeus.matcher.identifiermatcher;

import de.tum.med.aiim.markusbudeus.matcher.provider.IdentifierProvider;

public class ExactMatcher extends IdentifierMatcher<String> {

	public ExactMatcher(IdentifierProvider<String> provider) {
		super(provider);
	}

	@Override
	public Match<String> findMatch(String searchTerm) {
		return new SingleMatch<>(identifiers.get(searchTerm));
	}

}
