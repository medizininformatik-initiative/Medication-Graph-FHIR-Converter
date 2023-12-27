package de.tum.med.aiim.markusbudeus.matcher.resulttransformer;

import de.tum.med.aiim.markusbudeus.matcher.HouselistEntry;
import de.tum.med.aiim.markusbudeus.matcher.provider.IdentifierTarget;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface Filter extends ResultTransformer {

	@Override
	default List<Set<IdentifierTarget>> batchTransform(List<IdentifierTarget> targets, HouselistEntry entry) {
		List<Boolean> passedFilter = batchPassesFilter(targets, entry);
		List<Set<IdentifierTarget>> result = new ArrayList<>(targets.size());
		for (int i = 0; i < targets.size(); i++) {
			result.add(passedFilter.get(i) ? Set.of(targets.get(i)) : Set.of());
		}
		return result;
	}

	@Override
	default Set<IdentifierTarget> transform(IdentifierTarget target, HouselistEntry entry) {
		return passesFilter(target, entry) ? Set.of(target) : Set.of();
	}

	default List<Boolean> batchPassesFilter(List<IdentifierTarget> targets, HouselistEntry entry) {
		return targets.stream().map(target -> passesFilter(target, entry)).collect(Collectors.toList());
	}

	boolean passesFilter(IdentifierTarget target, HouselistEntry entry);

}
