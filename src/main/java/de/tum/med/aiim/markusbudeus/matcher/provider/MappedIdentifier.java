package de.tum.med.aiim.markusbudeus.matcher.provider;

import de.tum.med.aiim.markusbudeus.matcher.model.MatchingTarget;

import java.util.Objects;

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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		MappedIdentifier<?> that = (MappedIdentifier<?>) o;
		return Objects.equals(identifier, that.identifier) && Objects.equals(target, that.target);
	}

	@Override
	public int hashCode() {
		return Objects.hash(identifier, target);
	}
}
