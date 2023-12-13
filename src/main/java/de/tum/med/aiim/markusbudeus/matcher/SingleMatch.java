package de.tum.med.aiim.markusbudeus.matcher;

import de.tum.med.aiim.markusbudeus.matcher.provider.Identifier;

import java.util.Set;

public class SingleMatch<S> implements Match<S> {

	public final Identifier<S> match;

	public SingleMatch(Identifier<S> match) {
		this.match = match;
	}

	@Override
	public Set<Identifier<S>> getBestMatches() {
		if (match == null) return Set.of();
		return Set.of(match);
	}
}
