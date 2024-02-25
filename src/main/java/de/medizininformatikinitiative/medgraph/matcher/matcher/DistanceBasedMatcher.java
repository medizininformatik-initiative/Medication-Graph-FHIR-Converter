package de.medizininformatikinitiative.medgraph.matcher.matcher;

import de.medizininformatikinitiative.medgraph.matcher.provider.IdentifierProvider;
import de.medizininformatikinitiative.medgraph.matcher.provider.MappedIdentifier;
import de.medizininformatikinitiative.medgraph.matcher.matcher.model.ScoreMultiMatch;

import java.util.HashMap;
import java.util.Map;

/**
 * Superclass used by matchers which rank results by calculating a distance between the search term and possible
 * matches. Lower distances result in higher scores.
 *
 * @param <S> the type of search term and target objects used
 * @author Markus Budeus
 */
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
