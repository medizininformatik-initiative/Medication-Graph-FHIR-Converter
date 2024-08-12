package de.medizininformatikinitiative.medgraph.searchengine.tools;

import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	/**
	 * Checks equality of the two lists, while ignoring order. The two lists are equal iff they have the same elements
	 * with the same cardinalities.
	 */
	public static <S> boolean equalsIgnoreOrder(List<S> list1, List<S> list2) {
		return list1.size() == list2.size() && toCardinalityMap(list1).equals(toCardinalityMap(list2));
	}

	/**
	 * Returns a map which contains all elements of the given list as keys, associated with their corresponding
	 * cardinality in the list.
	 */
	private static <S> Map<S, Integer> toCardinalityMap(List<S> list) {
		Map<S, Integer> cardinalityMap = new HashMap<>();
		for (S value : list) {
			cardinalityMap.merge(value, 1, (old, one) -> old + 1);
		}
		return cardinalityMap;
	}

}
