package de.tum.med.aiim.markusbudeus.matcher2.algorithms;

import de.tum.med.aiim.markusbudeus.matcher2.HouselistEntry;
import de.tum.med.aiim.markusbudeus.matcher2.data.SubSortingTree;
import de.tum.med.aiim.markusbudeus.matcher2.model.MatchingTarget;

import java.util.List;

public interface MatchingAlgorithm {

	/**
	 * Attempts to find the best matches for the given house list entry. Returns the best matches in a deterministic
	 * order, with the best match first.
	 */
	SubSortingTree<MatchingTarget> match(HouselistEntry entry);

}
