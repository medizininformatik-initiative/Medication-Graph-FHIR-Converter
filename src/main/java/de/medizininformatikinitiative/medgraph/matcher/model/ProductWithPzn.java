package de.medizininformatikinitiative.medgraph.matcher.model;

/**
 * An extension of {@link Product}, which also contains a matching PZN.
 *
 * @author Markus Budeus
 */
public class ProductWithPzn extends Product {

	private final String pzn;

	public ProductWithPzn(long mmiId, String name, String pzn) {
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
