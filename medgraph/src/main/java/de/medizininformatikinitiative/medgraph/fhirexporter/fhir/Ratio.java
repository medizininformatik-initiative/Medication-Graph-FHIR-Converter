package de.medizininformatikinitiative.medgraph.fhirexporter.fhir;

/**
 * This class is an implementation of the FHIR R4 Ratio object.
 *
 * @author Markus Budeus
 */
public record Ratio(Quantity numerator, Quantity denominator) implements RatioOrQuantity {

	public Ratio {
		if (numerator == null || denominator == null) {
			throw new NullPointerException("The numerator and denominator may not be null!");
		}
	}

	@Override
	public Ratio plus(RatioOrQuantity other) {
		Quantity resultNumerator = null;
		Quantity resultDenominator = null;
		if (other instanceof Quantity) {
			resultNumerator = numerator.plus((Quantity) other);
			resultDenominator = denominator;
		} else if (other instanceof Ratio) {
			resultNumerator = numerator.plus(((Ratio) other).numerator);
			resultDenominator = denominator.plus(((Ratio) other).denominator);
		}

		if (resultNumerator != null && resultDenominator != null) {
			return new Ratio(resultNumerator, resultDenominator);
		}
		return null;
	}

}
