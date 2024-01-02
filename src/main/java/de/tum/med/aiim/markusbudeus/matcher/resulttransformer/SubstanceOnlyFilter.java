package de.tum.med.aiim.markusbudeus.matcher.resulttransformer;

import de.tum.med.aiim.markusbudeus.matcher.model.HouselistEntry;
import de.tum.med.aiim.markusbudeus.matcher.model.MatchingTarget;

public class SubstanceOnlyFilter implements Filter {
	@Override
	public boolean passesFilter(MatchingTarget target, HouselistEntry entry) {
		return target.getType() == MatchingTarget.Type.SUBSTANCE;
	}
}
