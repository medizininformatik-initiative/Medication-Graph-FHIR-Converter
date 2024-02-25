package de.medizininformatikinitiative.medgraph.matcher.judge;

import de.medizininformatikinitiative.medgraph.matcher.model.HouselistEntry;
import de.medizininformatikinitiative.medgraph.matcher.model.MatchingTarget;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementations of this interface assign a score to an existing match by some metric.
 *
 * @author Markus Budeus
 */
public interface MatchJudge {

	default List<Double> batchJudge(List<MatchingTarget> targetList, HouselistEntry entry) {
		List<Double> resultList = new ArrayList<>(targetList.size());
		targetList.forEach(t -> resultList.add(judge(t, entry)));
		return resultList;
	}

	double judge(MatchingTarget target, HouselistEntry entry);

}
