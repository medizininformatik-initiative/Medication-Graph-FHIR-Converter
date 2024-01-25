package de.tum.med.aiim.markusbudeus.matcher.model;

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
