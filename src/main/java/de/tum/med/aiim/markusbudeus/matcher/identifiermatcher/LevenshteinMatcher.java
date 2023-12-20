package de.tum.med.aiim.markusbudeus.matcher.identifiermatcher;

import de.tum.med.aiim.markusbudeus.matcher.provider.IdentifierProvider;
import org.apache.commons.text.similarity.LevenshteinDistance;

public class LevenshteinMatcher extends DistanceBasedMatcher<String> {

	private final LevenshteinDistance algorithm = new LevenshteinDistance(2);

	public LevenshteinMatcher(IdentifierProvider<String> provider) {
		super(provider);
	}

	@Override
	public Integer calculateDistance(String searchTerm, String target) {
		Integer result = algorithm.apply(searchTerm, target);
		if (result == -1) return null;
		return result;
	}

}
