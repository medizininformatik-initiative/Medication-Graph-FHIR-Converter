package de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject;

import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Original match found in the first step of the matching algorithm.
 *
 * @author Markus Budeus
 */
public class OriginalMatch<T extends Matchable> extends MatchingObject<T> {

	/**
	 * The origin where this match originated from.
	 */
	@NotNull
	private final Origin origin;

	/**
	 * Creates a new {@link MatchingObject} which manages the given {@link Matchable}.
	 *
	 * @param object the object to be managed by this instance
	 */
	public OriginalMatch(T object) {
		this(object, Origin.UNKNOWN);
	}

	/**
	 * Creates a new {@link MatchingObject} which manages the given {@link Matchable}.
	 *
	 * @param object the object to be managed by this instance
	 * @param origin the source this match originated from
	 */
	public OriginalMatch(@NotNull T object, @NotNull Origin origin) {
		super(object);
		this.origin = origin;
	}

	@NotNull
	public Origin getOrigin() {
		return origin;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		if (!super.equals(object)) return false;
		OriginalMatch<?> that = (OriginalMatch<?>) object;
		return Objects.equals(origin, that.origin);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), origin);
	}
}
