package de.tum.med.aiim.markusbudeus.matcher.identifiermatcher;

import de.tum.med.aiim.markusbudeus.matcher.provider.MappedIdentifier;
import de.tum.med.aiim.markusbudeus.matcher.provider.IdentifierProvider;

import java.util.*;

public class JaccardMatcher extends IdentifierMatcher<Set<String>> {

	public JaccardMatcher(IdentifierProvider<Set<String>> provider) {
		super(provider);
	}

	@Override
	public ScoreMultiMatch<Set<String>> findMatch(Set<String> searchTerm) {
		Collection<MappedIdentifier<Set<String>>> allIdentifiers = identifiers.values();
		List<ScoreMultiMatch.MatchWithScore<Set<String>>> scores = new ArrayList<>();

		allIdentifiers.forEach(identifier -> {
			double jaccard = getJaccardCoefficient(searchTerm, identifier.identifier);
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
