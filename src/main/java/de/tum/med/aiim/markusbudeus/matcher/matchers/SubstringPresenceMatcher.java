package de.tum.med.aiim.markusbudeus.matcher.matchers;

import java.util.Set;

/**
 * Counts how many of the given strings are found in the target string.
 */
public class SubstringPresenceMatcher extends ScoreBasedMatcher<Set<String>, String> {

	@Override
	public double getScore(Set<String> searchTerm, String target) {
		int matches = 0;
		for (String s: searchTerm) {
			if (target.contains(s)) matches++;
		}
		return matches;
	}

}
