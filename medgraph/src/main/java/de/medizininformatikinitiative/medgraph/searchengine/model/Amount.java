package de.medizininformatikinitiative.medgraph.searchengine.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * An amount is a number combined with a unit.
 *
 * @author Markus Budeus
 */
public class Amount {

	public final BigDecimal number;
	public final String unit;

	public Amount(BigDecimal number, String unit) {
		this.number = number;
		this.unit = unit;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Amount amount = (Amount) o;
		return Objects.equals(number, amount.number) && Objects.equals(unit, amount.unit);
	}

	@Override
	public int hashCode() {
		return Objects.hash(number, unit);
	}

	@Override
	public String toString() {
		return number + (unit != null ? unit : "");
	}
}