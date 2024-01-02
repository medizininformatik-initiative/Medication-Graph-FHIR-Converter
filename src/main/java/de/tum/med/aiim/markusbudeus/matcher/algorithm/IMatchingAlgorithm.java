package de.tum.med.aiim.markusbudeus.matcher.algorithm;

import de.tum.med.aiim.markusbudeus.matcher.MatchingResult;
import de.tum.med.aiim.markusbudeus.matcher.model.HouselistEntry;

public interface IMatchingAlgorithm {

	/**
	 * Attempts to find the best matches for the given house list entry.
	 */
	MatchingResult match(HouselistEntry entry);

}
