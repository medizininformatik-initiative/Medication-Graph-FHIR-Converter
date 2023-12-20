package de.tum.med.aiim.markusbudeus.matcher.identifiermatcher;

import de.tum.med.aiim.markusbudeus.matcher.provider.IdentifierProvider;
import de.tum.med.aiim.markusbudeus.matcher.provider.MappedIdentifier;

import java.util.HashMap;
import java.util.Map;

public abstract class DistanceBasedMatcher<S> extends IdentifierMatcher<S> {

	public DistanceBasedMatcher(IdentifierProvider<S> provider) {
		super(provider);
	}

	@Override
	public ScoreMultiMatch<S> findMatch(S searchTerm) {
		Map<MappedIdentifier<S>, Integer> distances = new HashMap<>();
		this.identifiers.values().forEach(identifier -> {
			Integer distance = calculateDistance(searchTerm, identifier.identifier.getIdentifier());
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
