package de.tum.med.aiim.markusbudeus.matcher.model;

public class FinalMatchingTarget extends Product {

	private final String pzn;

	public FinalMatchingTarget(long mmiId, String name, String pzn) {
		super(mmiId, name);
		this.pzn = pzn;
	}

	public String getPzn() {
		return pzn;
	}

	@Override
	public String toString() {
		return super.toString() + " [PZN="+pzn+"]";
	}
}
