package de.tum.med.aiim.markusbudeus.matcher.stringmatcher;

import de.tum.med.aiim.markusbudeus.matcher.ScoreMultiMatch;
import de.tum.med.aiim.markusbudeus.matcher.provider.Identifier;
import de.tum.med.aiim.markusbudeus.matcher.provider.IdentifierProvider;

import java.util.*;

public class JaccardMatcher extends StringMatcher<Set<String>> {

	public JaccardMatcher(IdentifierProvider<Set<String>> provider) {
		super(provider);
	}

	@Override
	public ScoreMultiMatch<Set<String>> findMatch(Set<String> name) {
		Collection<Identifier<Set<String>>> allIdentifiers = identifiers.values();
		List<ScoreMultiMatch.MatchWithScore<Set<String>>> scores = new ArrayList<>();

		allIdentifiers.forEach(identifier -> {
			double jaccard = getJaccardCoefficient(name, identifier.identifier);
			if (jaccard > 0) scores.add(new ScoreMultiMatch.MatchWithScore<>(identifier, jaccard));
		});
		return new ScoreMultiMatch<>(scores);
	}

	static double getJaccardCoefficient(Set<String> set1, Set<String> set2) {
		Set<String> intersection = new HashSet<>(set1);
		intersection.removeIf(o -> !set2.contains(o));
		Set<String> union = new HashSet<>(set1);
		union.addAll(set2);
		return 1.0 * intersection.size() / union.size();
	}

}
