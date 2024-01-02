package de.tum.med.aiim.markusbudeus.matcher;

import de.tum.med.aiim.markusbudeus.matcher.model.HouselistEntry;
import de.tum.med.aiim.markusbudeus.matcher.model.MatchingTarget;

import java.util.List;

public class MatchingResult {

	public final HouselistEntry searchTerm;
	public final MatchingTarget topResult;
	public final List<MatchingTarget> goodResults;
	public final List<MatchingTarget> otherResults;

	public MatchingResult(HouselistEntry searchTerm, MatchingTarget topResult, List<MatchingTarget> goodResults,
	                      List<MatchingTarget> otherResults) {
		this.searchTerm = searchTerm;
		this.topResult = topResult;
		this.goodResults = goodResults;
		this.otherResults = otherResults;
	}

}
