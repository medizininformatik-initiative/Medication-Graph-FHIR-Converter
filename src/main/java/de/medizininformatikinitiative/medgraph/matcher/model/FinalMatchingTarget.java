package de.medizininformatikinitiative.medgraph.matcher.model;

import java.util.List;

/**
 * An extension {@link ProductWithPzn}, this class features detailed information about the ingredients of a product.
 *
 * @author Markus Budeus
 */
public class FinalMatchingTarget extends ProductWithPzn {

	public final List<Drug> drugs;

	public FinalMatchingTarget(long mmiId, String name, String pzn, List<Drug> drugs) {
		super(mmiId, name, pzn);
		this.drugs = drugs;
	}

}
