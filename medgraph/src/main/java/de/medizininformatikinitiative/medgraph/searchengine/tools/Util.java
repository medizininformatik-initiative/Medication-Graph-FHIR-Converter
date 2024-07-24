package de.medizininformatikinitiative.medgraph.searchengine.tools;

/**
 * @author Markus Budeus
 */
public class Util {

	/**
	 * Converts the given distance into a score between 0 and 1. The larger the distance, the lower the score.
	 * @param distance the distance to convert to a score
	 * @return a score assigned to the distance
	 * @throws IllegalArgumentException if the distance is less than zero
	 */
	public static double distanceToScore(double distance) {
		if (distance < 0) throw new IllegalArgumentException("The distance must be positive! (Was "+distance+")");
		return 1 / (1 + distance);
	}

}
