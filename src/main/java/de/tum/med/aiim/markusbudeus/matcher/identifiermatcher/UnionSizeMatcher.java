package de.tum.med.aiim.markusbudeus.matcher.identifiermatcher;

import de.tum.med.aiim.markusbudeus.matcher.provider.IdentifierProvider;

import java.util.HashSet;
import java.util.Set;

public class UnionSizeMatcher extends ScoreBasedMatcher<Set<String>> {

	public UnionSizeMatcher(IdentifierProvider<Set<String>> provider) {
		super(provider);
	}

	@Override
	public double getScore(Set<String> searchTerm, Set<String> target) {
		return getUnionSize(searchTerm, target);
	}

	static double getUnionSize(Set<String> set1, Set<String> set2) {
		Set<String> intersection = new HashSet<>(set1);
		intersection.removeIf(o -> !set2.contains(o));

		// If it only matches on a number, ignore the match
		int deduction = 0;
		for (String inBoth : intersection) {
			if (isOnlyDigits(inBoth)) deduction++;
		}
		return intersection.size() - deduction;
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
