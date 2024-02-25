package de.medizininformatikinitiative.medgraph.matcher.model;

import java.util.List;

/**
 * Represents a Drug, which is part of a pharmaceutical product.
 *
 * @author Markus Budeus
 */
public class Drug {

	public final String doseForm;
	public final String edqmDoseForm;
	public final Amount amount;

	public final List<ActiveIngredient> activeIngredients;

	public Drug(String doseForm, String edqmDoseForm, Amount amount, List<ActiveIngredient> activeIngredients) {
		this.doseForm = doseForm;
		this.edqmDoseForm = edqmDoseForm;
		this.amount = amount;
		this.activeIngredients = activeIngredients;
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();

		if (amount.unit != null)
			stringBuilder.append(amount).append(" ");
		stringBuilder.append(doseForm);
		if (edqmDoseForm != null) {
			stringBuilder.append(" (").append(edqmDoseForm).append(")");
		}

		for (ActiveIngredient ingredient: activeIngredients) {
			stringBuilder.append("\n    ").append(ingredient.toString());
		}
		return stringBuilder.toString();
	}
}
