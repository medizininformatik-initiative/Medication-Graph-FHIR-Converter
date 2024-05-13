package de.medizininformatikinitiative.medgraph.searchengine.matcher;

import org.apache.commons.text.similarity.LevenshteinDistance;

/**
 * This matcher calculates the Levensthein distance between two strings, accepting matches if the distance is two or
 * less.
 *
 * @author Markus Budeus
 */
public class LevenshteinMatcher extends DistanceBasedMatcher<String> {

	private final LevenshteinDistance algorithm = new LevenshteinDistance(2);

	@Override
	public Integer calculateDistance(String searchTerm, String target) {
		Integer result = algorithm.apply(searchTerm, target);
		if (result == -1) return null;
		return result;
	}

	@Override
	protected boolean supportsParallelism() {
		// The LevenshteinDistance.apply function only writes local variables, so parallel execution is fine
		return true;
	}
}
