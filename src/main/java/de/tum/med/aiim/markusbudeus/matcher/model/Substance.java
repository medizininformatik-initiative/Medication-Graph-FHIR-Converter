package de.tum.med.aiim.markusbudeus.matcher.model;

public class Substance extends MatchingTarget {

	public Substance(long mmiId, String name) {
		super(mmiId, name);
	}

	@Override
	public Type getType() {
		return Type.SUBSTANCE;
	}
}
