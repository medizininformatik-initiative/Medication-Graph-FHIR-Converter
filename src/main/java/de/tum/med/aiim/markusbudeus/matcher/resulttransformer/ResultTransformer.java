package de.tum.med.aiim.markusbudeus.matcher.resulttransformer;

import de.tum.med.aiim.markusbudeus.matcher.HouselistEntry;
import de.tum.med.aiim.markusbudeus.matcher.provider.IdentifierTarget;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementations transform identifier targets to new, different identifier targets. For example resolving a substance
 * target into a set of product targets.
 */
public interface ResultTransformer {

	default List<Set<IdentifierTarget>> batchTransform(List<IdentifierTarget> targets, HouselistEntry entry) {
		return targets.stream().map(target -> transform(target, entry)).collect(Collectors.toList());
	}

	Set<IdentifierTarget> transform(IdentifierTarget target, HouselistEntry entry);

}
