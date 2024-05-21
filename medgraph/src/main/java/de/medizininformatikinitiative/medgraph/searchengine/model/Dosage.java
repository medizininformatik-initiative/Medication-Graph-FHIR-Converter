package de.medizininformatikinitiative.medgraph.searchengine.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * A dosage, which may only be a nominator (e.g., "15 mg"), a nominator and a denominator (e.g., "15mg/ml") or a
 * nominator with a qualifier and a denominator (e.g., "15mg Iron/ml").
 *
 * @author Markus Budeus
 */
public class Dosage {

	/**
	 * Construcs a dosage using an amount given as string (with '.' as decimal separator) and a unit.
	 *
	 * @param amount the amount as string
	 * @param unit   the unit
	 * @return the dosage instance
	 * @throws NumberFormatException if the given amount is not a valid representation of a {@link BigDecimal}
	 */
	public static Dosage of(@NotNull String amount, @NotNull String unit) {
		return Dosage.of(new BigDecimal(amount), unit);
	}

	/**
	 * Constructs a dosage using an integer amount and a unit.
	 */
	public static Dosage of(int amount, @NotNull String unit) {
		return Dosage.of(new BigDecimal(amount), unit);
	}

	/**
	 * Constructs a dosage using an amount and a unit.
	 */
	public static Dosage of(@NotNull BigDecimal amount, @NotNull String unit) {
		return new Dosage(new Amount(amount, unit), null, null);
	}

	/**
	 * Constructs a ratio dosage with a nominator and a denominator.
	 *
	 * @param nominatorAmount   the nominator amount
	 * @param nominatorUnit     the nominator's unit
	 * @param denominatorAmount the denominator's amount
	 * @param denominatorUnit   the denominator's unit
	 * @return the constructed {@link Dosage}
	 */
	public static Dosage of(@NotNull BigDecimal nominatorAmount, @NotNull String nominatorUnit,
	                        @NotNull BigDecimal denominatorAmount, @NotNull String denominatorUnit) {
		return new Dosage(new Amount(nominatorAmount, nominatorUnit), null,
				new Amount(denominatorAmount, denominatorUnit));
	}

	@NotNull
	public final Amount amountNominator;
	/**
	 * An additional qualifier for the nominator's amount. For example, if the dosage were "1mg Iron/1ml", the qualifier
	 * would be "Iron".
	 */
	@Nullable
	public final String nominatorQualifier;
	@Nullable
	public final Amount amountDenominator;

	public Dosage(@NotNull Amount amountNominator, @Nullable String nominatorQualifier,
	              @Nullable Amount amountDenominator) {
		this.amountNominator = amountNominator;
		this.nominatorQualifier = nominatorQualifier;
		this.amountDenominator = amountDenominator;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Dosage dosage = (Dosage) o;
		return Objects.equals(amountNominator, dosage.amountNominator) && Objects.equals(
				nominatorQualifier, dosage.nominatorQualifier) && Objects.equals(amountDenominator,
				dosage.amountDenominator);
	}

	@Override
	public int hashCode() {
		return Objects.hash(amountNominator, nominatorQualifier, amountDenominator);
	}

	@Override
	public String toString() {
		String nominator = nominatorQualifier != null ? amountNominator + " " + nominatorQualifier : amountNominator.toString();
		String denominator = amountDenominator != null ?
				(amountDenominator.number.equals(BigDecimal.ONE) ? amountDenominator.unit : amountDenominator.toString())
				: null;
		return denominator != null ? nominator + "/" + denominator : nominator;
	}
}
