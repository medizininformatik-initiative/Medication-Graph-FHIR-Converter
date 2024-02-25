package de.medizininformatikinitiative.medgraph.matcher.resulttransformer;

import de.medizininformatikinitiative.medgraph.matcher.model.HouselistEntry;
import de.medizininformatikinitiative.medgraph.matcher.model.MatchingTarget;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public interface Filter extends ResultTransformer {

	@Override
	default List<List<MatchingTarget>> batchTransform(List<MatchingTarget> targets, HouselistEntry entry) {
		List<Boolean> passedFilter = batchPassesFilter(targets, entry);
		List<List<MatchingTarget>> result = new ArrayList<>(targets.size());
		for (int i = 0; i < targets.size(); i++) {
			result.add(passedFilter.get(i) ? List.of(targets.get(i)) : List.of());
		}
		return result;
	}

	@Override
	default List<MatchingTarget> transform(MatchingTarget target, HouselistEntry entry) {
		return passesFilter(target, entry) ? List.of(target) : List.of();
	}

	default List<Boolean> batchPassesFilter(List<MatchingTarget> targets, HouselistEntry entry) {
		return targets.stream().map(target -> passesFilter(target, entry)).collect(Collectors.toList());
	}

	boolean passesFilter(MatchingTarget target, HouselistEntry entry);

}
