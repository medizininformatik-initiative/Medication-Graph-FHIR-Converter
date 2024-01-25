package de.tum.med.aiim.markusbudeus.matcher.model;

import java.util.List;

public class FinalMatchingTarget extends ProductWithPzn {

	public final List<Drug> drugs;

	public FinalMatchingTarget(long mmiId, String name, String pzn, List<Drug> drugs) {
		super(mmiId, name, pzn);
		this.drugs = drugs;
	}

}
