package de.tum.med.aiim.markusbudeus.matcher.resultranker;

import de.tum.med.aiim.markusbudeus.matcher.HouselistEntry;
import de.tum.med.aiim.markusbudeus.matcher.model.MatchingTarget;

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
