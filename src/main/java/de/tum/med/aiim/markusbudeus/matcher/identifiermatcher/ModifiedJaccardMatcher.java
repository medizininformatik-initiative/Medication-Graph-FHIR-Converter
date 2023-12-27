package de.tum.med.aiim.markusbudeus.matcher.identifiermatcher;

import de.tum.med.aiim.markusbudeus.matcher.provider.IdentifierProvider;

import java.util.HashSet;
import java.util.Set;

/**
 * Calculates the Jaccard coefficient as score for matching between two string sets.
 * However, as a small modification: If two sets of strings only match on a single string and that single string
 * only consits of digits, it is not considered to be a match.
 */
public class ModifiedJaccardMatcher extends ScoreBasedMatcher<Set<String>> {

	public ModifiedJaccardMatcher(IdentifierProvider<Set<String>> provider) {
		super(provider);
	}

	@Override
	public double getScore(Set<String> searchTerm, Set<String> target) {
		return getJaccardCoefficient(searchTerm, target);
	}

	static double getJaccardCoefficient(Set<String> set1, Set<String> set2) {
		Set<String> intersection = new HashSet<>(set1);
		intersection.removeIf(o -> !set2.contains(o));
		Set<String> union = new HashSet<>(set1);
		union.addAll(set2);

		if (set2.contains("desferal")) {
			System.err.println("What?");
		}

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

}
