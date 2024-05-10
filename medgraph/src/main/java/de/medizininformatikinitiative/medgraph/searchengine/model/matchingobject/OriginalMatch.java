package de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject;

/**
 * Original match found in the first step of the matching algorithm.
 *
 * @author Markus Budeus
 */
public class OriginalMatch extends MatchingObject {

	/**
	 * Creates a new {@link MatchingObject} which manages the given {@link Matchable}.
	 *
	 * @param object the object to be managed by this instance
	 */
	public OriginalMatch(Matchable object) {
		super(object);
	}

}
