package de.tum.med.aiim.markusbudeus.matcher2.matchers;

import de.tum.med.aiim.markusbudeus.matcher.provider.IdentifierProvider;
import de.tum.med.aiim.markusbudeus.matcher2.HouselistEntry;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.function.Function;

public class LevenshteinMatcher extends DistanceBasedMatcher<String> {

	private final LevenshteinDistance algorithm = new LevenshteinDistance(2);

	@Override
	public Integer calculateDistance(String searchTerm, String target) {
		Integer result = algorithm.apply(searchTerm, target);
		if (result == -1) return null;
		return result;
	}

}
