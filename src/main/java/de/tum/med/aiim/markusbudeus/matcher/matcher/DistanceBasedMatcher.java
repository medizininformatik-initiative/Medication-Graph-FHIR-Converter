package de.tum.med.aiim.markusbudeus.matcher.matcher;

import de.tum.med.aiim.markusbudeus.matcher.matcher.model.ScoreMultiMatch;
import de.tum.med.aiim.markusbudeus.matcher.provider.IdentifierProvider;
import de.tum.med.aiim.markusbudeus.matcher.provider.MappedIdentifier;

import java.util.HashMap;
import java.util.Map;

public abstract class DistanceBasedMatcher<S> extends SimpleMatcher<S, ScoreMultiMatch<S>> {

	@Override
	protected ScoreMultiMatch<S> findMatch(S searchTerm, IdentifierProvider<S> identifierProvider) {
		Map<MappedIdentifier<S>, Integer> distances = new HashMap<>();
		identifierProvider.getIdentifiers().forEach(identifier -> {
			Integer distance = calculateDistance(searchTerm, identifier.identifier);
			if (distance != null) distances.put(identifier, distance);
		});
		return ScoreMultiMatch.scoreByDistance(distances);
	}

	/**
	 * Calculates the distance between the search term and the target. May return null to indicate that they shall not
	 * be included in the result.
	 */
	public abstract Integer calculateDistance(S searchTerm, S target);

}
