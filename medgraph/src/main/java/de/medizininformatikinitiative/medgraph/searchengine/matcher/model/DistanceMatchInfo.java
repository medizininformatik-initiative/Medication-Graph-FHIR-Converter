package de.medizininformatikinitiative.medgraph.searchengine.matcher.model;

import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.TrackableIdentifier;
import de.medizininformatikinitiative.medgraph.searchengine.provider.MappedIdentifier;
import org.jetbrains.annotations.NotNull;

/**
 * Information about match which has a given distance to the search term based on whatever metric the corresponding
 * matcher uses. Implements comparable, but greater distances are considered to be "less".
 *
 * @author Markus Budeus
 */
public class DistanceMatchInfo implements MatchInfo, Comparable<DistanceMatchInfo> {
	private final double distance;

	/**
	 * Creates a new distance match info.
	 *
	 * @param distance the distance between the identifier and the search term according to the matcher's metric
	 */
	public DistanceMatchInfo(double distance) {
		this.distance = distance;
	}

	/**
	 * Returns the distance between the identifier and the used search term.
	 */
	public double getDistance() {
		return distance;
	}

	@Override
	public int compareTo(@NotNull DistanceMatchInfo o) {
		return Double.compare(o.distance, distance); // Reverse ordering! Greater distance is "worth" less
	}
}
