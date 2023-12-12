package de.tum.med.aiim.markusbudeus.matcher;

public class SynonymeTarget {

	public final long mmiId;
	public final String name;
	public final Type type;

	public SynonymeTarget(long mmiId, String name, Type type) {
		this.mmiId = mmiId;
		this.name = name;
		this.type = type;
	}

	public enum Type {
		PRODUCT,
		SUBSTANCE
	}

}
