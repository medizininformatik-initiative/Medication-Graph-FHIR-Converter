package de.medizininformatikinitiative.medgraph.matcher.matcher;

import java.util.Set;

/**
 * Counts how many of the given strings are found in the target string.
 *
 * @author Markus Budeus
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
