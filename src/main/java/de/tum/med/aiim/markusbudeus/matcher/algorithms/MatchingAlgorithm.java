package de.tum.med.aiim.markusbudeus.matcher.algorithms;

import de.tum.med.aiim.markusbudeus.matcher.HouselistEntry;
import de.tum.med.aiim.markusbudeus.matcher.provider.IdentifierTarget;

import java.util.List;

public interface MatchingAlgorithm {

	/**
	 * Attempts to find the best matches for the given house list entry. Returns the best matches in a deterministic
	 * order, with the best match first.
	 */
	List<IdentifierTarget> match(HouselistEntry entry);

}
