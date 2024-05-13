package de.medizininformatikinitiative.medgraph.searchengine.matcher;

import de.medizininformatikinitiative.medgraph.searchengine.matcher.model.ScoreBasedMatch;
import de.medizininformatikinitiative.medgraph.searchengine.provider.MappedIdentifier;

import java.util.Set;

/**
 * Counts how many of the given strings are found in the target string.
 *
 * @author Markus Budeus
 */
public class SubstringPresenceMatcher extends Matcher<String, Set<String>, ScoreBasedMatch<String>> {

	@Override
	protected ScoreBasedMatch<String> match(Set<String> searchTerm, MappedIdentifier<String> identifier) {
		double score = getScore(searchTerm, identifier.identifier);
		if (score > 0) return new ScoreBasedMatch<>(identifier, score);
		return null;
	}

	static double getScore(Set<String> searchTerm, String target) {
		int matches = 0;
		for (String s: searchTerm) {
			if (target.contains(s)) matches++;
		}
		return matches;
	}

	@Override
	protected boolean supportsParallelism() {
		return true;
	}
}
