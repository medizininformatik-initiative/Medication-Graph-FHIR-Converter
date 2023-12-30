package de.tum.med.aiim.markusbudeus.matcher2;

import de.tum.med.aiim.markusbudeus.matcher2.data.SubSortingTree;
import de.tum.med.aiim.markusbudeus.matcher2.houselisttransformer.HouselistTransformer;
import de.tum.med.aiim.markusbudeus.matcher2.model.MatchingTarget;
import de.tum.med.aiim.markusbudeus.matcher2.resulttransformer.ResultTransformer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OngoingMatching {

	private final HouselistEntry entry;
	private SubSortingTree<MatchingTarget> currentMatches;

	public OngoingMatching(HouselistEntry entry, List<MatchingTarget> initialMatches) {
		this.entry = entry;
		this.currentMatches = new SubSortingTree<>(initialMatches);
	}

	public void transformHouselistEntry(HouselistTransformer transformer) {
		transformer.transform(entry);
	}

	public void transformResults(ResultTransformer resultTransformer) {
		transformResults(resultTransformer, false);
	}

	/**
	 * Transforms the current {@link MatchingTarget}s using the given {@link ResultTransformer}.
	 * @param resultTransformer the result transformer to apply
	 * @param onlyIfNotEmpty if true, the transformation will not be applied if it would eliminate all {@link MatchingTarget}s
	 * @return true if the transformation was applied, false if onlyIfNotEmpty was true and caused the transformation
	 * to not be applied
	 */
	public boolean transformResults(ResultTransformer resultTransformer, boolean onlyIfNotEmpty) {
		Map<MatchingTarget, List<MatchingTarget>> transformationResults = new HashMap<>();
		List<MatchingTarget> allTargets = currentMatches.getContents();
		List<List<MatchingTarget>> transformedMatchingTargets = resultTransformer.batchTransform(allTargets, entry);
		for (int i = 0; i < allTargets.size(); i++) {
			transformationResults.put(allTargets.get(i), transformedMatchingTargets.get(i));
		}

		if (onlyIfNotEmpty) {
			boolean empty = true;
			for (List<MatchingTarget> targets : transformedMatchingTargets) {
				if (!targets.isEmpty()) {
					empty = false;
					break;
				}
			}
			if (empty)
				return false;
		}

		currentMatches.batchReplace(transformationResults);
		currentMatches.clearDuplicates();
		return true;
	}

	/**
	 * The houselist entry currently being matched.
	 */
	public HouselistEntry getHouselistEntry() {
		return entry;
	}

	public List<MatchingTarget> getCurrentMatches() {
		return currentMatches.getContents();
	}

}
