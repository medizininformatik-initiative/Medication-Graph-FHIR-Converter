package de.medizininformatikinitiative.medgraph.matcher.matcher;

import java.util.HashSet;
import java.util.Set;

/**
 * Matches sets of strings by calculating the size of the union of those sets. Distinct sets are excluded from the
 * result.
 *
 * @author Markus Budeus
 */
public class UnionSizeMatcher extends ScoreBasedMatcher<Set<String>, Set<String>> {

	@Override
	public double getScore(Set<String> searchTerm, Set<String> target) {
		return getUnionSize(searchTerm, target);
	}

	static double getUnionSize(Set<String> set1, Set<String> set2) {
		Set<String> intersection = new HashSet<>(set1);
		intersection.removeIf(o -> !set2.contains(o));
		return intersection.size();
	}

}
