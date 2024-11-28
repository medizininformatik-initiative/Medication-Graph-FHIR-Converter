package de.medizininformatikinitiative.medgraph.searchengine.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.*;

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

	// Ensure that the constructors do not allow the correspondences to recursively contain this instance.
	// This can be achieved by never providing outside write access to the correspondences set, except during object
	// creation.

	/**
	 * Contains active ingredients that this ingredient effectively amounts to. For example, if this instance represents
	 * midazolam hydrochloride, the correspondences contain midazolam with a lower dosage. If this instance represents
	 * sodium flouride, the correspondences may contain the respective amounts of sodium and flouride.
	 */
	@NotNull
	private final Set<ActiveIngredient> correspondences;

	public ActiveIngredient(@NotNull String substanceName, @Nullable AmountOrRange amount) {
		this(substanceName, amount, new HashSet<>());
	}

	public ActiveIngredient(@NotNull String substanceName, @Nullable AmountOrRange amount,
	                        @NotNull Set<ActiveIngredient> correspondences) {
		this.substanceName = substanceName;
		this.amount = amount;
		this.correspondences = new HashSet<>(correspondences);
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

	/**
	 * Returns what other ingredients this instance effectively corresponds to.
	 */
	public @NotNull Set<ActiveIngredient> getCorrespondences() {
		return new HashSet<>(correspondences);
	}

	@Override
	public String toString() {
		String prefix = amount + " " + substanceName;
		if (correspondences.isEmpty()) {
			return prefix;
		}

		// Best-effort sorting. Corresponding ingredients with higher masses are listed first.
		List<ActiveIngredient> orderedCorrespondences = new ArrayList<>(correspondences);
		orderedCorrespondences.sort(Comparator.<ActiveIngredient, BigDecimal>comparing(c -> {
			AmountOrRange a = c.amount;
			if (a == null) {
				return BigDecimal.ZERO;
			} else if (a instanceof Amount) {
				return ((Amount) a).getNumber();
			} else {
				return ((AmountRange) a).getFrom();
			}
		}).reversed());

		return prefix + " " + Arrays.toString(orderedCorrespondences.toArray());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ActiveIngredient that = (ActiveIngredient) o;
		return Objects.equals(substanceName, that.substanceName)
				&& Objects.equals(amount, that.amount)
				&& correspondences.equals(that.correspondences);
	}

	@Override
	public int hashCode() {
		return Objects.hash(substanceName, amount);
	}
}
