package de.tum.med.aiim.markusbudeus.matcher.stringmatcher;

import de.tum.med.aiim.markusbudeus.matcher.provider.Identifier;
import de.tum.med.aiim.markusbudeus.matcher.provider.IdentifierProvider;

import java.util.Map;

public abstract class StringMatcher<S> implements IStringMatcher<S> {

	protected final IdentifierProvider<S> provider;
	protected final Map<S, Identifier<S>> identifiers;

	public StringMatcher(IdentifierProvider<S> provider) {
		this.provider = provider;
		identifiers = provider.getIdentifiers();
	}

	@Override
	public S transform(String name) {
		return provider.applyTransformation(name);
	}
}
