package de.medizininformatikinitiative.medgraph.matcher.resulttransformer;

import de.medizininformatikinitiative.medgraph.matcher.model.HouselistEntry;
import de.medizininformatikinitiative.medgraph.matcher.model.MatchingTarget;

/**
 * Filter which only product matches pass.
 *
 * @author Markus Budeus
 */
public class ProductOnlyFilter implements Filter {
	@Override
	public boolean passesFilter(MatchingTarget target, HouselistEntry entry) {
		return target.getType() == MatchingTarget.Type.PRODUCT;
	}
}
