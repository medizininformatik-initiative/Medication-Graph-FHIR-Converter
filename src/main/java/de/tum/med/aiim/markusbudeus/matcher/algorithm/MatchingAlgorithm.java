package de.tum.med.aiim.markusbudeus.matcher.algorithm;

import de.tum.med.aiim.markusbudeus.matcher.data.SubSortingTree;
import de.tum.med.aiim.markusbudeus.matcher.model.HouselistEntry;
import de.tum.med.aiim.markusbudeus.matcher.model.MatchingTarget;

public interface MatchingAlgorithm {

	/**
	 * Attempts to find the best matches for the given house list entry.
	 */
	SubSortingTree<MatchingTarget> match(HouselistEntry entry);

}
