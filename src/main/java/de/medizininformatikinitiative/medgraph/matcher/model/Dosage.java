package de.medizininformatikinitiative.medgraph.matcher.model;

import java.util.Objects;

/**
 * A dosage, which may only be a nominator (e.g., "15 mg"), a nominator and a denominator (e.g., "15mg/ml") or a
 * nominator with a qualifier and a denominator (e.g., "15mg Iron/ml").
 *
 * @author Markus Budeus
 */
public class Dosage {

	public final Amount amountNominator;
	/**
	 * An additional qualifier for the nominator's amount. For example, if the dosage were "1mg Iron/1ml", the
	 * qualifier would be "Iron".
	 */
	public final String nominatorQualifier;
	public final Amount amountDenominator;

	public Dosage(Amount amountNominator, String nominatorQualifier, Amount amountDenominator) {
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
		return amountDenominator != null ? nominator + "/" + amountDenominator : nominator;
	}
}
