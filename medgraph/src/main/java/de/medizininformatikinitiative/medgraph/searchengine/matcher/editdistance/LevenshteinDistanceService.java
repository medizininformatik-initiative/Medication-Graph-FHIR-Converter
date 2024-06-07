package de.medizininformatikinitiative.medgraph.searchengine.matcher.editdistance;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.OptionalInt;

/**
 * Limited or unlimited Levenshtein distance calculation service.
 *
 * @author Markus Budeus
 */
public class LevenshteinDistanceService implements EditDistanceService {

	private final LevenshteinDistance distance;

	/**
	 * Unlimited Levenshtein distance calculator.
	 */
	public LevenshteinDistanceService() {
		distance = new LevenshteinDistance();
	}

	/**
	 * Limited Levenshtein distance calculator. If the edit distance between two words passed to
	 * {@link #apply(String, String)} exceeds the threshold, {@link OptionalInt#empty()} is returned.
	 *
	 * @param threshold the threshold to use
	 */
	public LevenshteinDistanceService(int threshold) {
		distance = new LevenshteinDistance(threshold);
	}

	@Override
	public OptionalInt apply(String left, String right) {
		int distance = this.distance.apply(left, right);
		if (distance == -1) return OptionalInt.empty();
		return OptionalInt.of(distance);
	}
}
