package de.medizininformatikinitiative.medgraph.searchengine.matcher;

import org.apache.commons.text.similarity.LevenshteinDistance;

/**
 * Class which caches {@link LevenshteinDistance}-instances.
 *
 * @author Markus Budeus
 */
public class LevenshteinDistanceProvider {

	private static final LevenshteinDistance[] cachedProviders = new LevenshteinDistance[4];

	public static LevenshteinDistance getInstance(int threshold) {
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

}
