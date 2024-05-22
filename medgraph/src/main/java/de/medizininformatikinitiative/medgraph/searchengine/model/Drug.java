package de.medizininformatikinitiative.medgraph.searchengine.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * Represents a Drug, which is part of a pharmaceutical product.
 *
 * @author Markus Budeus
 */
public class Drug {

	@Nullable
	private final String doseForm;
	@Nullable
	private final String edqmDoseForm;
	@Nullable
	private final Amount amount;

	@NotNull
	private final List<ActiveIngredient> activeIngredients;

	public Drug(@Nullable String doseForm, @Nullable String edqmDoseForm, @Nullable Amount amount,
	            @NotNull List<ActiveIngredient> activeIngredients) {
		this.doseForm = doseForm;
		this.edqmDoseForm = edqmDoseForm;
		this.amount = amount;
		this.activeIngredients = activeIngredients;
	}

	/**
	 * Returns the MMI dose form of thos drug.
	 */
	@Nullable
	public String getDoseForm() {
		return doseForm;
	}

	/**
	 * Returns the EDQM dose form of this drug or null if none exists or is known.
	 */
	@Nullable
	public String getEdqmDoseForm() {
		return edqmDoseForm;
	}

	/**
	 * Returns the amount of this drug. (I.e. 10ml if it's an ampoule.)
	 */
	@Nullable
	public Amount getAmount() {
		return amount;
	}

	/**
	 * Returns the active ingredients of this drug.
	 */
	@NotNull
	public List<ActiveIngredient> getActiveIngredients() {
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Drug drug = (Drug) o;
		return Objects.equals(doseForm, drug.doseForm) && Objects.equals(edqmDoseForm,
				drug.edqmDoseForm) && Objects.equals(amount, drug.amount) && Objects.equals(
				activeIngredients, drug.activeIngredients);
	}

	@Override
	public int hashCode() {
		return Objects.hash(doseForm, edqmDoseForm, amount, activeIngredients);
	}
}
