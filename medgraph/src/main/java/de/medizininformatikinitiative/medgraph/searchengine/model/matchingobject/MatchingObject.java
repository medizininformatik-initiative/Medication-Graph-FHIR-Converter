package de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject;

import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;

/**
 * Carrier class for a {@link Matchable} with an assigned score.
 *
 * @author Markus Budeus
 */
public interface MatchingObject<S extends Matchable> {

	/**
	 * Returns the object held by this carrier class.
	 */
	S getObject();

	/**
	 * Returns the current score assigned to the object carried by this class.
	 */
	double getScore();

}
