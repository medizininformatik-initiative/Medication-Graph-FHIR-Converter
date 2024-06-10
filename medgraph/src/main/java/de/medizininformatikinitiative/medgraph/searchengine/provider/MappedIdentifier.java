package de.medizininformatikinitiative.medgraph.searchengine.provider;

import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Identifiable;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.Identifier;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.OriginalIdentifier;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * This is an {@link Identifier} mapped to a specific {@link Identifiable}, which is whatever the corresponding
 * identifier describes. For example, the {@link Identifiable} may be a substance and the identifer is a known name for
 * this substance. Of course, multiple identifiers (and thus, multiple instance of this class) may exist for a single
 * identifiable object.
 *
 * @author Markus Budeus
 */
public class MappedIdentifier<S> {

	@NotNull
	public final Identifier<S> identifier;
	@NotNull
	public final Identifiable target;

	/**
	 * Creates a new {@link MappedIdentifier}, whose {@link Identifier} has the source
	 * {@link OriginalIdentifier.Source#KNOWN_IDENTIFIER}.
	 *
	 * @param identifier the identifier
	 * @param target     the identifiable which is identified by the identifier
	 */
	public MappedIdentifier(@NotNull S identifier, @NotNull Identifiable target) {
		this(new OriginalIdentifier<>(identifier, OriginalIdentifier.Source.KNOWN_IDENTIFIER), target);
	}

	public MappedIdentifier(@NotNull Identifier<S> identifier, @NotNull Identifiable target) {
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
