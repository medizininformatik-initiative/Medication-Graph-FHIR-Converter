package de.medizininformatikinitiative.medgraph.searchengine.matcher;

import java.util.HashSet;
import java.util.Set;

/**
 * Matches sets of strings by calculating the size of the union of those sets. Distinct sets are excluded from the
 * result.
 *
 * @author Markus Budeus
 */
public class UnionSizeMatcher extends ScoreBasedMatcher<Set<String>> {

	@Override
	public double calculateScore(Set<String> searchTerm, Set<String> target) {
		return getUnionSize(searchTerm, target);
	}

	static double getUnionSize(Set<String> set1, Set<String> set2) {
		Set<String> intersection = new HashSet<>(set1);
		intersection.removeIf(o -> !set2.contains(o));
		return intersection.size();
	}

	@Override
	protected boolean supportsParallelism() {
		return true;
	}
}
