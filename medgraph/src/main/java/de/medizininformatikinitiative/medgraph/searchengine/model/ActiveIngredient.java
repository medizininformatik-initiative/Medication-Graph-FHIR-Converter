package de.medizininformatikinitiative.medgraph.searchengine.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Represents an active ingredient of a drug.
 *
 * @author Markus Budeus
 */
public class ActiveIngredient {

	/**
	 * The substance name of this ingredient.
	 */
	@NotNull
	private final String substanceName;
	/**
	 * The ingredient's amount.
	 */
	@Nullable
	private final AmountOrRange amount;

	public ActiveIngredient(@NotNull String substanceName, @Nullable AmountOrRange amount) {
		this.substanceName = substanceName;
		this.amount = amount;
	}

	/**
	 * Returns the substance name of this active ingredient.
	 */
	@NotNull
	public String getSubstanceName() {
		return substanceName;
	}

	/**
	 * Returns the amount of this active ingredient, if known.
	 */
	@Nullable
	public AmountOrRange getAmount() {
		return amount;
	}

	@Override
	public String toString() {
		return amount + " " + substanceName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ActiveIngredient that = (ActiveIngredient) o;
		return Objects.equals(substanceName, that.substanceName) && Objects.equals(amount, that.amount);
	}

	@Override
	public int hashCode() {
		return Objects.hash(substanceName, amount);
	}
}
