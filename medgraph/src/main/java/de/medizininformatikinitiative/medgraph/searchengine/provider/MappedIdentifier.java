package de.medizininformatikinitiative.medgraph.searchengine.provider;

import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Identifiable;

import java.util.Objects;

/**
 * This is an identifier mapped to a specific {@link Identifiable}, which is whatever the corresponding identifier
 * describes. For example, the {@link Identifiable} may be a substance and the identifer is a known name for this
 * substance. Of course, multiple identifiers (and thus, multiple instance of this class) may exist for a single
 * identifiable object.
 *
 * @author Markus Budeus
 */
public class MappedIdentifier<S> {

	public final S identifier;
	public final Identifiable target;

	public MappedIdentifier(S identifier, Identifiable target) {
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
