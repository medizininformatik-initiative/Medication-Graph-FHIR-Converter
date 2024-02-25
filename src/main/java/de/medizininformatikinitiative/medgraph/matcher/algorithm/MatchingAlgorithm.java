package de.medizininformatikinitiative.medgraph.matcher.algorithm;

import de.medizininformatikinitiative.medgraph.matcher.model.MatchingTarget;
import de.medizininformatikinitiative.medgraph.matcher.data.SubSortingTree;
import de.medizininformatikinitiative.medgraph.matcher.model.HouselistEntry;

/**
 * A matching algorithm implementation which looks up a house list entry.
 *
 * @author Markus Budeus
 */
public interface MatchingAlgorithm {

	/**
	 * Attempts to find the best matches for the given house list entry.
	 */
	SubSortingTree<MatchingTarget> match(HouselistEntry entry);

}
