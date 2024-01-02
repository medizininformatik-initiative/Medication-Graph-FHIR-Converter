package de.tum.med.aiim.markusbudeus.matcher.algorithm;

import de.tum.med.aiim.markusbudeus.matcher.MatchingResult;
import de.tum.med.aiim.markusbudeus.matcher.data.SubSortingTree;
import de.tum.med.aiim.markusbudeus.matcher.model.HouselistEntry;
import de.tum.med.aiim.markusbudeus.matcher.model.MatchingTarget;

import java.util.List;

public abstract class MatchingAlgorithm implements IMatchingAlgorithm {

	@Override
	public MatchingResult match(HouselistEntry entry) {
		if (entry == null)
			throw new NullPointerException("The given houselist entry may not be null!");

		SubSortingTree<MatchingTarget> resultTree = matchInternal(entry);

		List<MatchingTarget> results = resultTree.getContents();
		if (results.isEmpty()) {
			return new MatchingResult(entry, null, List.of(), List.of());
		}

		List<MatchingTarget> topResults = resultTree.getTopContents();
		results = results.subList(topResults.size(), results.size());
		MatchingTarget best = selectBest(topResults);
		topResults.remove(best);
		return new MatchingResult(entry, best, topResults, results);
	}

	protected abstract SubSortingTree<MatchingTarget> matchInternal(HouselistEntry entry);

	protected abstract MatchingTarget selectBest(List<MatchingTarget> list);

}
