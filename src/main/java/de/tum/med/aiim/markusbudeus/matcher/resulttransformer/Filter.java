package de.tum.med.aiim.markusbudeus.matcher.resulttransformer;

import de.tum.med.aiim.markusbudeus.matcher.HouselistEntry;
import de.tum.med.aiim.markusbudeus.matcher.provider.IdentifierTarget;
import org.neo4j.driver.Record;

import java.util.Set;

public interface Filter extends ResultTransformer {

	@Override
	default Set<IdentifierTarget> transform(IdentifierTarget target, HouselistEntry entry) {
		return passesFilter(target, entry) ? Set.of(target) : Set.of();
	}

	boolean passesFilter(IdentifierTarget target, HouselistEntry entry);

}
