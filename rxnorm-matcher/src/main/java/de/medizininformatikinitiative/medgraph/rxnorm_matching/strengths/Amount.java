package de.medizininformatikinitiative.medgraph.rxnorm_matching.strengths;

import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * An amount is a number, possibly combined with a unit. Please note that instances may be given as {@link AmountRange},
 * which specifies a less exact amount.
 *
 * @author Markus Budeus
 */
public final class Amount implements AmountOrRange, Matchable {

	/**
	 * The amount number.
	 */
	@NotNull
	private final BigDecimal number;
	/**
	 * The unit of this amount.
	 */
	@NotNull
	private final Unit unit;

	public Amount(@NotNull BigDecimal number, @NotNull Unit unit) {
		if (number == null) throw new NullPointerException("The number may not be null!");
		if (unit == null) throw new NullPointerException("The unit may not be null!");
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
	@Override
	@NotNull
	public Unit getUnit() {
		return unit;
	}

	@Override
	public boolean containsOrEquals(Amount amount, BigDecimal delta) {
		return Objects.equals(getUnit(), amount.getUnit()) &&
				number.add(delta).compareTo(amount.getNumber()) >= 0
				&& number.subtract(delta).compareTo(amount.getNumber()) <= 0;
	}

	@Override
	public boolean equalsWithRelativeDelta(AmountOrRange other, BigDecimal relativeDelta) {
		if (other instanceof Amount a) {
			return containsOrEquals(a, relativeDelta.multiply(getNumber()));
		} else return other.containsOrEquals(this, relativeDelta);
	}

	@Override
	public Amount multiply(Amount other) {
		return new Amount(getNumber().multiply(other.getNumber()), unit.multiply(other.getUnit()));
	}

	@Override
	public Amount divide(Amount other) {
		return new Amount(getNumber().divide(other.getNumber(), 10, RoundingMode.HALF_UP), unit.divide(other.getUnit()));
	}

	@Override
	public Amount setUnit(Unit unit) {
		return new Amount(number, unit);
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
		String unitString = unit.toString();
		return number + (unitString.isBlank() ? "" : " " + unitString);
	}

	@Override
	public String getName() {
		return toString();
	}
}
