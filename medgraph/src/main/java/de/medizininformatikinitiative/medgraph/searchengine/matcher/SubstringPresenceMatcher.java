package de.medizininformatikinitiative.medgraph.searchengine.matcher;

import de.medizininformatikinitiative.medgraph.searchengine.matcher.model.ScoreMatchInfo;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.TrackableIdentifier;
import de.medizininformatikinitiative.medgraph.searchengine.provider.MappedIdentifier;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Counts how many of the given strings are found in the target string.
 *
 * @author Markus Budeus
 */
public class SubstringPresenceMatcher extends ExtendedMatcher<Set<String>, String, ScoreMatchInfo> {

	@Override
	protected @Nullable ScoreMatchInfo match(Set<String> searchTerm, String target) {
		double score = getScore(searchTerm, target);
		if (score > 0) return new ScoreMatchInfo(score);
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
