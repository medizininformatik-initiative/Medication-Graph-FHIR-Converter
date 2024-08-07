package de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject;

import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;
import org.jetbrains.annotations.NotNull;

/**
 * Carrier class for a {@link Matchable} with an assigned score. This class is {@link Comparable} and the default
 * sorting order is the highest score come first.
 *
 * @author Markus Budeus
 */
public interface MatchingObject<S extends Matchable> extends Comparable<MatchingObject<S>> {

	/**
	 * Returns the object held by this carrier class.
	 */
	S getObject();

	/**
	 * Returns the current score assigned to the object carried by this class.
	 */
	double getScore();

	@Override
	default int compareTo(@NotNull MatchingObject<S> o) {
		return Double.compare(o.getScore(), getScore());
	}
}
