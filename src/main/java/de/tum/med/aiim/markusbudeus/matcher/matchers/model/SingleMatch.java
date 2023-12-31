package de.tum.med.aiim.markusbudeus.matcher.matchers.model;

import de.tum.med.aiim.markusbudeus.matcher.provider.MappedIdentifier;

import java.util.Set;

public class SingleMatch<S> implements Match<S> {

	public final MappedIdentifier<S> match;

	public SingleMatch(MappedIdentifier<S> match) {
		this.match = match;
	}

	@Override
	public Set<MappedIdentifier<S>> getBestMatches() {
		if (match == null) return Set.of();
		return Set.of(match);
	}
}
