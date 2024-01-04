package de.tum.med.aiim.markusbudeus.matcher.matcher;

import java.util.HashSet;
import java.util.Set;

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
