package de.medizininformatikinitiative.medgraph.searchengine.matcher;

import java.util.HashSet;
import java.util.Set;

/**
 * Calculates the Jaccard coefficient as score for matching between two string sets.
 * However, as a small modification: If two sets of strings only match on a single string and that single string
 * only consits of digits, it is not considered to be a match.
 *
 * @author Markus Budeus
 */
public class ModifiedJaccardMatcher extends ScoreBasedMatcher<Set<String>> {

	@Override
	public double calculateScore(Set<String> searchTerm, Set<String> target) {
		return getJaccardCoefficient(searchTerm, target);
	}

	static double getJaccardCoefficient(Set<String> set1, Set<String> set2) {
		Set<String> union = new HashSet<>(set1);
		union.addAll(set2);
		if (union.isEmpty()) return 0;
		Set<String> intersection = new HashSet<>(set1);
		intersection.removeIf(o -> !set2.contains(o));

		// If it only matches on a digit, drop the match
		if (intersection.size() == 1) {
			String inBoth = intersection.iterator().next();
			if (isOnlyDigits(inBoth)) return 0;
		}

		return 1.0 * intersection.size() / union.size();
	}

	private static boolean isOnlyDigits(String string) {
		boolean onlyDigits = true;
		for (int i = 0; i < string.length(); i++) {
			if (!Character.isDigit(string.charAt(i))) {
				onlyDigits = false;
				break;
			}
		}
		return onlyDigits;
	}

	@Override
	protected boolean supportsParallelism() {
		return true;
	}
}
