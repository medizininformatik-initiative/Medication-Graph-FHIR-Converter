package de.tum.med.aiim.markusbudeus.matcher2.model;

public class Product extends MatchingTarget {

	public Product(long mmiId, String name) {
		super(mmiId, name);
	}

	@Override
	public Type getType() {
		return Type.PRODUCT;
	}
}
