package de.tum.med.aiim.markusbudeus.matcher.algorithms;

import de.tum.med.aiim.markusbudeus.matcher.HouselistEntry;
import de.tum.med.aiim.markusbudeus.matcher.data.SubSortingTree;
import de.tum.med.aiim.markusbudeus.matcher.model.MatchingTarget;

public interface MatchingAlgorithm {

	/**
	 * Attempts to find the best matches for the given house list entry. Returns the best matches in a deterministic
	 * order, with the best match first.
	 */
	SubSortingTree<MatchingTarget> match(HouselistEntry entry);

}
