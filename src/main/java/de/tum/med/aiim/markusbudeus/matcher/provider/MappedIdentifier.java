package de.tum.med.aiim.markusbudeus.matcher.provider;

import de.tum.med.aiim.markusbudeus.matcher.model.MatchingTarget;

/**
 * This is an identifier mapped to a specific target, which is whatever the corresponding identifier
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
