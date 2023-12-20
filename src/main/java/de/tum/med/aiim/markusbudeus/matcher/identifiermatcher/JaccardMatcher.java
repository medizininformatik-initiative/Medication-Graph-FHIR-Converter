package de.tum.med.aiim.markusbudeus.matcher.identifiermatcher;

import de.tum.med.aiim.markusbudeus.matcher.provider.IdentifierProvider;

import java.util.HashSet;
import java.util.Set;

public class JaccardMatcher extends ScoreBasedMatcher<Set<String>> {

	public JaccardMatcher(IdentifierProvider<Set<String>> provider) {
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
		return 1.0 * intersection.size() / union.size();
	}

}
