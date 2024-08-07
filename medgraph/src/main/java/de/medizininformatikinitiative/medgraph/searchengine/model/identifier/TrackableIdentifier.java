package de.medizininformatikinitiative.medgraph.searchengine.model.identifier;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Wraps an identifier (i.e. name) of something. Usually a string that may or may not have been subjected through
 * various transformations. This wrapper can then hold information about where the (actual) identifier came from.
 * Instances of this class are immutable.
 *
 * @param <T> the type of this identifier
 * @author Markus Budeus
 */
public abstract class TrackableIdentifier<T> implements Identifier<T> {

	/**
	 * The actual identifier wrapped by this instance.
	 */
	@NotNull
	private final T identifier;

	protected TrackableIdentifier(@NotNull T identifier) {
		this.identifier = identifier;
	}

	@NotNull
	public T getIdentifier() {
		return identifier;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		TrackableIdentifier<?> that = (TrackableIdentifier<?>) object;
		return Objects.equals(identifier, that.identifier);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(identifier);
	}

	@Override
	public String toString() {
		return identifier.toString();
	}
}
