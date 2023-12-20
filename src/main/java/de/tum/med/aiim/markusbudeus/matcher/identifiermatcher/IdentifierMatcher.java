package de.tum.med.aiim.markusbudeus.matcher.identifiermatcher;

import de.tum.med.aiim.markusbudeus.matcher.provider.MappedIdentifier;
import de.tum.med.aiim.markusbudeus.matcher.provider.IdentifierProvider;

import java.util.Map;

public abstract class IdentifierMatcher<S> implements IIdentifierMatcher<S> {

	protected final IdentifierProvider<S> provider;
	protected final Map<S, MappedIdentifier<S>> identifiers;

	public IdentifierMatcher(IdentifierProvider<S> provider) {
		this.provider = provider;
		identifiers = provider.getIdentifiers();
	}

	@Override
	public S transform(String name) {
		return provider.applyTransformation(name);
	}
}
