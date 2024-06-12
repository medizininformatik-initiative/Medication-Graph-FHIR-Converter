package de.medizininformatikinitiative.medgraph.searchengine.model;

import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.EdqmPharmaceuticalDoseForm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents a Drug, which is part of a pharmaceutical product.
 *
 * @author Markus Budeus
 */
public record Drug(@Nullable String doseForm, @Nullable EdqmPharmaceuticalDoseForm edqmDoseForm,
                   @Nullable Amount amount, @NotNull List<ActiveIngredient> activeIngredients) {

	/**
	 * Returns the MMI dose form of thos drug.
	 */
	@Override
	@Nullable
	public String doseForm() {
		return doseForm;
	}

	/**
	 * Returns the EDQM dose form of this drug or null if none exists or is known.
	 */
	@Override
	@Nullable
	public EdqmPharmaceuticalDoseForm edqmDoseForm() {
		return edqmDoseForm;
	}

	/**
	 * Returns the amount of this drug. (I.e. 10ml if it's an ampoule.)
	 */
	@Override
	@Nullable
	public Amount amount() {
		return amount;
	}

	/**
	 * Returns the active ingredients of this drug.
	 */
	@Override
	@NotNull
	public List<ActiveIngredient> activeIngredients() {
		return activeIngredients;
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();

		if (amount != null && amount.getUnit() != null)
			stringBuilder.append(amount).append(" ");
		stringBuilder.append(doseForm);
		if (edqmDoseForm != null) {
			stringBuilder.append(" (").append(edqmDoseForm).append(")");
		}

		for (ActiveIngredient ingredient : activeIngredients) {
			stringBuilder.append("\n    ").append(ingredient.toString());
		}
		return stringBuilder.toString();
	}

}
