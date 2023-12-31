package de.tum.med.aiim.markusbudeus.matcher2;

import de.tum.med.aiim.markusbudeus.matcher2.data.SubSortingTree;
import de.tum.med.aiim.markusbudeus.matcher2.model.MatchingTarget;

public class MatchingResult {

	public final HouselistEntry searchTerm;
	public final SubSortingTree<MatchingTarget> result;

	public MatchingResult(HouselistEntry searchTerm, SubSortingTree<MatchingTarget> result) {
		this.searchTerm = searchTerm;
		this.result = result;
	}

}
