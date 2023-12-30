package de.tum.med.aiim.markusbudeus.matcher2.resulttransformer;

import de.tum.med.aiim.markusbudeus.matcher2.HouselistEntry;
import de.tum.med.aiim.markusbudeus.matcher2.model.MatchingTarget;

public class ProductOnlyFilter implements Filter {
	@Override
	public boolean passesFilter(MatchingTarget target, HouselistEntry entry) {
		return target.getType() == MatchingTarget.Type.PRODUCT;
	}
}
