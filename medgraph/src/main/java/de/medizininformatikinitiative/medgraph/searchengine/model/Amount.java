package de.medizininformatikinitiative.medgraph.searchengine.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * An amount is a number, possibly combined with a unit. Please note that instances may be given as {@link AmountRange},
 * which specifies a less exact amount.
 *
 * @author Markus Budeus
 */
public class Amount {

	/**
	 * The amount number.
	 */
	@NotNull
	private final BigDecimal number;
	/**
	 * The unit of this amount.
	 */
	@Nullable
	private final String unit;

	public Amount(@NotNull BigDecimal number, @Nullable String unit) {
		this.number = number;
		this.unit = unit;
	}


	/**
	 * Returns the number of this amount.
	 */
	@NotNull
	public BigDecimal getNumber() {
		return number;
	}

	/**
	 * Returns the unit of this amount.
	 */
	@Nullable
	public String getUnit() {
		return unit;
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
		return number + (unit != null ? " " + unit : "");
	}
}
