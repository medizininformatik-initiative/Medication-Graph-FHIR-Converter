package de.medizininformatikinitiative.medgraph.matcher.model;

/**
 * A product which is an intermediate match of the search algorithm.
 *
 * @author Markus Budeus
 */
public class Product extends MatchingTarget {

	public Product(long mmiId, String name) {
		super(mmiId, name);
	}

	@Override
	public Type getType() {
		return Type.PRODUCT;
	}
}
