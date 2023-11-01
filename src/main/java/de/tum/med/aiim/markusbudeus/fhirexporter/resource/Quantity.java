package de.tum.med.aiim.markusbudeus.fhirexporter.resource;

import java.math.BigDecimal;

public class Quantity implements RatioOrQuantity {

	public BigDecimal value;
	private Code comparator;
	public String unit;
	public Uri system;
	/**
	 * A computer processable form of the unit in some unit representation system.
	 */
	public Code code;

	public Code getComparator() {
		return comparator;
	}

	public void setComparator(Comparator comparator) {
		if (comparator == null || comparator == Comparator.EXACT) {
			this.comparator = null;
		} else {
			this.comparator = new Code(comparator.value);
		}
	}

	private enum Comparator {
		EXACT("="),
		LESS("<"),
		LESS_OR_EQUAL("<="),
		GREATER_OR_EQUAL(">="),
		GREATER(">");

		private final String value;

		Comparator(String value) {
			this.value = value;
		}
	}

}
