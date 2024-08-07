package de.medizininformatikinitiative.medgraph.searchengine.provider;

import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Identifiable;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.Identifier;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.TrackableIdentifier;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.OriginalIdentifier;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * This is an {@link TrackableIdentifier} mapped to a specific {@link Identifiable}, which is whatever the corresponding
 * identifier describes. For example, the {@link Identifiable} may be a substance and the identifer is a known name for
 * this substance. Of course, multiple identifiers (and thus, multiple instance of this class) may exist for a single
 * identifiable object.
 *
 * @author Markus Budeus
 */
public class MappedIdentifier<S, T extends Identifiable> implements Identifier<S> {

	@NotNull
	public final TrackableIdentifier<S> trackableIdentifier;
	@NotNull
	public final T target;

	/**
	 * Creates a new {@link MappedIdentifier}, whose {@link TrackableIdentifier} has the source
	 * {@link OriginalIdentifier.Source#KNOWN_IDENTIFIER}.
	 *
	 * @param trackableIdentifier the identifier
	 * @param target     the identifiable which is identified by the identifier
	 */
	public MappedIdentifier(@NotNull S trackableIdentifier, @NotNull T target) {
		this(new OriginalIdentifier<>(trackableIdentifier, OriginalIdentifier.Source.KNOWN_IDENTIFIER), target);
	}

	public MappedIdentifier(@NotNull TrackableIdentifier<S> trackableIdentifier, @NotNull T target) {
		this.trackableIdentifier = trackableIdentifier;
		this.target = target;
	}

	@Override
	public String toString() {
		return trackableIdentifier + ": " + target;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		MappedIdentifier<?, ?> that = (MappedIdentifier<?, ?>) o;
		return Objects.equals(trackableIdentifier, that.trackableIdentifier) && Objects.equals(target, that.target);
	}

	@Override
	public int hashCode() {
		return Objects.hash(trackableIdentifier, target);
	}

	@Override
	public S getIdentifier() {
		return trackableIdentifier.getIdentifier();
	}
}
