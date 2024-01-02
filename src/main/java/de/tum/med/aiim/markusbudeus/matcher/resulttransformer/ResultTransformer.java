package de.tum.med.aiim.markusbudeus.matcher.resulttransformer;

import de.tum.med.aiim.markusbudeus.matcher.model.HouselistEntry;
import de.tum.med.aiim.markusbudeus.matcher.model.MatchingTarget;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementations transform identifier targets to new, different identifier targets. For example resolving a substance
 * target into a set of product targets.
 */
public interface ResultTransformer {

	default List<List<MatchingTarget>> batchTransform(List<MatchingTarget> targets, HouselistEntry entry) {
		return targets.stream().map(target -> transform(target, entry)).collect(Collectors.toList());
	}

	List<MatchingTarget> transform(MatchingTarget target, HouselistEntry entry);

}
