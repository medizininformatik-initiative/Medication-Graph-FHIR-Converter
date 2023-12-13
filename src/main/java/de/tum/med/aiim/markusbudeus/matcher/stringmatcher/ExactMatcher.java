package de.tum.med.aiim.markusbudeus.matcher.stringmatcher;

import de.tum.med.aiim.markusbudeus.matcher.Match;
import de.tum.med.aiim.markusbudeus.matcher.SingleMatch;
import de.tum.med.aiim.markusbudeus.matcher.provider.IdentifierProvider;

public class ExactMatcher extends StringMatcher<String> {

	public ExactMatcher(IdentifierProvider<String> provider) {
		super(provider);
	}

	@Override
	public Match<String> findMatch(String name) {
		return new SingleMatch<>(identifiers.get(name));
	}

}
