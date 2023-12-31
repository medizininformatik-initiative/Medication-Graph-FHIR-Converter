package de.tum.med.aiim.markusbudeus.matcher2.resultranker;

import de.tum.med.aiim.markusbudeus.matcher2.HouselistEntry;
import de.tum.med.aiim.markusbudeus.matcher2.model.MatchingTarget;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementations of this interface assign a score to an existing match by some metric.
 */
public interface MatchJudge {

	default List<Double> batchJudge(List<MatchingTarget> targetList, HouselistEntry entry) {
		List<Double> resultList = new ArrayList<>(targetList.size());
		targetList.forEach(t -> resultList.add(judge(t, entry)));
		return resultList;
	}

	double judge(MatchingTarget target, HouselistEntry entry);

}
