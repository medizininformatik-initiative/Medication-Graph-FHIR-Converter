package de.medizininformatikinitiative.medgraph.searchengine.tools;

import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject;

import java.util.List;

/**
 * @author Markus Budeus
 */
public class Util {

	/**
	 * Converts the given distance into a score between 0 and 1. The larger the distance, the lower the score.
	 *
	 * @param distance the distance to convert to a score
	 * @return a score assigned to the distance
	 * @throws IllegalArgumentException if the distance is less than zero
	 */
	public static double distanceToScore(double distance) {
		if (distance < 0) throw new IllegalArgumentException("The distance must be positive! (Was " + distance + ")");
		return 1 / (1 + distance);
	}

	/**
	 * Unpacks the given {@link MatchingObject MatchingObjects}, i.e. returns a list that contains the
	 * {@link Matchable}-instances held by the given objects in the same order as the input list.
	 *
	 * @param objectList the objects to unpack
	 * @param <S>        the type of objects held by the carrier classes
	 * @return a list of the {@link Matchable}-objects held by the carrier objects in the input list, with ordering
	 * preserved
	 */
	public static <S extends Matchable> List<S> unpack(List<? extends MatchingObject<S>> objectList) {
		return objectList.stream().map(MatchingObject::getObject).toList();
	}

}
