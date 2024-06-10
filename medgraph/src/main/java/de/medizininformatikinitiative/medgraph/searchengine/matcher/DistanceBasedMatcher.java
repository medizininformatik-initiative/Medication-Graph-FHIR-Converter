package de.medizininformatikinitiative.medgraph.searchengine.matcher;

import de.medizininformatikinitiative.medgraph.searchengine.matcher.model.DistanceBasedMatch;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.Identifier;
import de.medizininformatikinitiative.medgraph.searchengine.provider.MappedIdentifier;

/**
 * Superclass used by matchers which rank results by calculating a distance between the search term and possible
 * matches.
 *
 * @param <S> the type of search term and target objects used
 * @author Markus Budeus
 */
public abstract class DistanceBasedMatcher<S> extends SimpleMatcher<S, DistanceBasedMatch<S, S>> {

	@Override
	protected DistanceBasedMatch<S, S> match(Identifier<S> searchTerm, MappedIdentifier<S> mi) {
		Integer distance = calculateDistance(searchTerm.getIdentifier(), mi.identifier.getIdentifier());
		if (distance != null) return new DistanceBasedMatch<>(searchTerm, mi, distance);
		return null;
	}

	/**
	 * Calculates the distance between the search term and the target. May return null to indicate that they shall not
	 * be included in the result.
	 */
	public abstract Integer calculateDistance(S searchTerm, S target);

}
