package de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject;

import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Carrier object for {@link Matchable} during the matching process. Stores a global score assigned to this object. It
 * always starts with a {@link OriginalMatch}, which is then transitively referenced by other implementations of this
 * class to represents the different matching pipeline steps that were applied to the carried {@link Matchable}. See the
 * respective implementations for more information.
 * <p>
 * The base implementation of this class is immutable and so should be all implementing classes.
 *
 * @param <T> the type of object carried
 * @author Markus Budeus
 */
public abstract class MatchingObjectBase<T extends Matchable> implements MatchingObject<T> {

	@NotNull
	private final T object;
	/**
	 * The current score of this match.
	 */
	private final double score;

	/**
	 * Creates a new {@link MatchingObjectBase} which manages the given {@link Matchable}.
	 *
	 * @param object the object to be managed by this instance
	 */
	protected MatchingObjectBase(@NotNull T object, double score) {
		this.object = object;
		this.score = score;
	}

	/**
	 * Returns the {@link Matchable} managed by this instance.
	 */
	@NotNull
	public T getObject() {
		return object;
	}

	/**
	 * Returns the score assigned to this match at the current time.
	 */
	public double getScore() {
		return score;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) return true;
		if (other == null || getClass() != other.getClass()) return false;
		MatchingObjectBase<?> object1 = (MatchingObjectBase<?>) other;
		return Objects.equals(object, object1.object) && Objects.equals(score,
				object1.score);
	}

	@Override
	public int hashCode() {
		return Objects.hash(object, score);
	}
}
