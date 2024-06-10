package de.medizininformatikinitiative.medgraph.searchengine.matcher;

import de.medizininformatikinitiative.medgraph.searchengine.matcher.model.ScoreBasedMatch;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.Identifier;
import de.medizininformatikinitiative.medgraph.searchengine.provider.MappedIdentifier;

import java.util.Set;

/**
 * Counts how many of the given strings are found in the target string.
 *
 * @author Markus Budeus
 */
public class SubstringPresenceMatcher extends Matcher<Set<String>, String, ScoreBasedMatch<Set<String>, String>> {

	@Override
	protected ScoreBasedMatch<Set<String>, String> match(Identifier<Set<String>> searchTerm, MappedIdentifier<String> mi) {
		double score = getScore(searchTerm.getIdentifier(), mi.identifier.getIdentifier());
		if (score > 0) return new ScoreBasedMatch<>(searchTerm, mi, score);
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
