package de.tum.med.aiim.markusbudeus.matcher;

import de.tum.med.aiim.markusbudeus.matcher.identifiermatcher.Match;
import de.tum.med.aiim.markusbudeus.matcher.provider.IdentifierTarget;

import java.util.List;

public class MatchingResult {

	public final HouselistEntry searchTerm;
	public final List<IdentifierTarget> result;

	public MatchingResult(HouselistEntry searchTerm, List<IdentifierTarget> result) {
		this.searchTerm = searchTerm;
		this.result = result;
	}

}
