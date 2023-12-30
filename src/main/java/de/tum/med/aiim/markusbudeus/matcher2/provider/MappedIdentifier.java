package de.tum.med.aiim.markusbudeus.matcher2.provider;

import de.tum.med.aiim.markusbudeus.matcher.provider.Identifier;
import de.tum.med.aiim.markusbudeus.matcher.provider.MappedBaseIdentifier;
import de.tum.med.aiim.markusbudeus.matcher2.model.MatchingTarget;

import java.util.List;

/**
 * This is an {@link Identifier} mapped to a set of identifier targets, which are whatever the corresponding identifier
 * describes.
 */
public class MappedIdentifier<S> {

	public final S identifier;
	public final MatchingTarget target;

	public MappedIdentifier(S identifier, MatchingTarget target) {
		this.identifier = identifier;
		this.target = target;
	}

	@Override
	public String toString() {
		return identifier + ": " + target;
	}
}
