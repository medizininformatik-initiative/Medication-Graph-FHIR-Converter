package de.medizininformatikinitiative.medgraph.searchengine.matcher;

import de.medizininformatikinitiative.medgraph.searchengine.matcher.model.DistanceMatchInfo;
import org.jetbrains.annotations.Nullable;

/**
 * Superclass used by matchers which rank results by calculating a distance between the search term and possible
 * matches.
 *
 * @param <S> the type of search term and target objects used
 * @author Markus Budeus
 */
public abstract class DistanceBasedMatcher<S> extends SimpleMatcher<S, DistanceMatchInfo> {

	@Override
	protected @Nullable DistanceMatchInfo match(S searchTerm, S target) {
		Integer distance = calculateDistance(searchTerm, target);
		if (distance != null) return new DistanceMatchInfo(distance);
		return null;
	}

	/**
	 * Calculates the distance between the search term and the target. May return null to indicate that they shall not
	 * be included in the result.
	 */
	public abstract Integer calculateDistance(S searchTerm, S target);

}
