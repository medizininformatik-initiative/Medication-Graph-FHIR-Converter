package de.tum.med.aiim.markusbudeus.matcher.resulttransformer;

import de.tum.med.aiim.markusbudeus.matcher.HouselistEntry;
import de.tum.med.aiim.markusbudeus.matcher.provider.IdentifierTarget;

public class SubstanceOnlyFilter implements Filter {
	@Override
	public boolean passesFilter(IdentifierTarget target, HouselistEntry entry) {
		return target.type == IdentifierTarget.Type.SUBSTANCE;
	}
}
