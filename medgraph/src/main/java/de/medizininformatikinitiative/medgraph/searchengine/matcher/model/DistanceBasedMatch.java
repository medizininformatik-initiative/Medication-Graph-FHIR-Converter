//package de.medizininformatikinitiative.medgraph.searchengine.matcher.model;
//
//import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.TrackableIdentifier;
//import de.medizininformatikinitiative.medgraph.searchengine.provider.MappedIdentifier;
//
///**
// * A match which has a given distance to the search term based on whatever metric the corresponding matcher uses.
// *
// * @author Markus Budeus
// */
//public class DistanceBasedMatch<S, T> extends Match<S, T> {
//	private final double distance;
//
//	/**
//	 * Creates a new distance based match.
//	 *
//	 * @param matchedIdentifier the matched identifier
//	 * @param distance          the distance between the identifier and the search term according to the matcher's
//	 *                          metric
//	 */
//	public DistanceBasedMatch(TrackableIdentifier<S> searchTerm, MappedIdentifier<T> matchedIdentifier, double distance) {
//		super(searchTerm, matchedIdentifier);
//		this.distance = distance;
//	}
//
//	/**
//	 * Returns the distance between the identifier and the used search term.
//	 */
//	public double getDistance() {
//		return distance;
//	}
//}
