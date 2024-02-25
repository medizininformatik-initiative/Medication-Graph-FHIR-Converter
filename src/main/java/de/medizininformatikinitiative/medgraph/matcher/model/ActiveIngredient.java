package de.medizininformatikinitiative.medgraph.matcher.model;

/**
 * Represents an active ingredient of a drug.
 *
 * @author Markus Budeus
 */
public class ActiveIngredient {

	public final String substanceName;
	public final AmountRange amount;

	public ActiveIngredient(String substanceName, AmountRange amount) {
		this.substanceName = substanceName;
		this.amount = amount;
	}

	@Override
	public String toString() {
		return amount + " " + substanceName;
	}
}
