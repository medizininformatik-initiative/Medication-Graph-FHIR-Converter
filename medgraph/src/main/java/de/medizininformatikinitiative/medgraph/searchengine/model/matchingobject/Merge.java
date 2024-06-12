package de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject;

import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * In case multiple transformations result in the same {@link Matchable} being generated, they are merged to a single
 * {@link MatchingObject} to avoid duplicate processing. In this case, an instance of this class is constructed to
 * reference the original, duplicate {@link MatchingObject MatchingObjects}.
 *
 * @author Markus Budeus
 */
public class Merge<T extends Matchable> extends MatchingObject<T> {

	/**
	 * Verifies all entries in {@link #sourceObjects} reference the same {@link Matchable} and then returns the
	 * {@link Matchable} from the first entry of the list.
	 *
	 * @param sourceObjects the list to check
	 * @return the {@link Matchable} referenced by the first list entry
	 * @throws NullPointerException             if {@link #sourceObjects} is null
	 * @throws IllegalArgumentException         if {@link #sourceObjects} is empty or if the objects inside
	 *                                          {@link #sourceObjects} reference different {@link Matchable}s
	 */
	private static <T extends Matchable> T checkAndResolveMatchable(List<? extends MatchingObject<? extends T>> sourceObjects) {
		if (sourceObjects == null) throw new NullPointerException("The list of source objects may not be null!");
		if (sourceObjects.isEmpty()) throw new IllegalArgumentException("The list of source objects may not be empty!");
		T matchable = sourceObjects.getFirst().getObject();
		for (MatchingObject<? extends T> object : sourceObjects) {
			if (!object.getObject().equals(matchable)) throw new IllegalArgumentException("The given source objects do not all reference the same Matchable!");
		}
		return matchable;
	}

	@NotNull
	private final List<MatchingObject<? extends T>> sourceObjects;

	public Merge(List<? extends MatchingObject<? extends T>> sourceObjects) {
		super(checkAndResolveMatchable(sourceObjects));
		this.sourceObjects = new ArrayList<>(sourceObjects);
	}


	/**
	 * Returns the objects which were merged into this instance.
	 */
	@NotNull
	public List<MatchingObject<? extends T>> getSourceObjects() {
		return sourceObjects;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		Merge<?> merge = (Merge<?>) o;
		return Objects.equals(sourceObjects, merge.sourceObjects);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), sourceObjects);
	}
}
