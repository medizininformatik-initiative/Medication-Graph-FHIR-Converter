package de.medizininformatikinitiative.medgraph.searchengine.matcher.editdistance;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.OptionalInt;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Edit distance service which relies on Levenshtein distance, but selects the threshold to use based on the input.
 *
 * @author Markus Budeus
 */
public class FlexibleLevenshteinDistanceService implements EditDistanceService {

	private static final LevenshteinDistance[] cachedProviders = new LevenshteinDistance[4];

	private static LevenshteinDistance getInstance(int threshold) {
		if (threshold < cachedProviders.length) {
			LevenshteinDistance distance = cachedProviders[threshold];
			if (distance == null) {
				// No need to synchronize; If multiple threads were to do this concurrently, it won't matter.
				distance = cachedProviders[threshold] = new LevenshteinDistance(threshold);
			}
			return distance;
		}

		return new LevenshteinDistance(threshold);
	}

	private final BiFunction<String, String, Integer> thresholdSelector;

	/**
	 * With this constructor, you can pass in a function which selects the threshold of the Levenshtein distance to use
	 * based on the length of the longer one of the two input words.
	 */
	public FlexibleLevenshteinDistanceService(Function<Integer, Integer> thresholdSelector) {
		this.thresholdSelector = (s1, s2) -> thresholdSelector.apply(Math.max(s1.length(), s2.length()));
	}

	/**
	 * With this constructor, you can pass in a function which selects the threshold of the Levenshtein distance to use
	 * based on the two input words.
	 */
	public FlexibleLevenshteinDistanceService(BiFunction<String, String, Integer> thresholdSelector) {
		this.thresholdSelector = thresholdSelector;
	}

	@Override
	public OptionalInt apply(String left, String right) {
		LevenshteinDistance distanceCalculator = getInstance(thresholdSelector.apply(left, right));
		int distance = distanceCalculator.apply(left, right);
		if (distance == -1) return OptionalInt.empty();
		return OptionalInt.of(distance);
	}
}
