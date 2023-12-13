package de.tum.med.aiim.markusbudeus.matcher.provider;

public class IdentifierTarget {

	public final long mmiId;
	public final String name;
	public final Type type;

	public IdentifierTarget(long mmiId, String name, Type type) {
		this.mmiId = mmiId;
		this.name = name;
		this.type = type;
	}

	public enum Type {
		PRODUCT,
		SUBSTANCE
	}

}
