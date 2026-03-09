package de.medizininformatikinitiative.medgraph.rxnorm_matching.strengths;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

/**
 * Describes an amount of something, or possibly an amount range.
 *
 * @author Markus Budeus
 */
public sealed interface AmountOrRange permits Amount, AmountRange {

	/**
	 * The unit of this amount specification or null if no unit is specified.
	 */
	@NotNull
	Unit getUnit();

	/**
	 * If this is a range, returns whether the given amount lies within the range <b>and</b> the units match. If this is
	 * a plain value, returns whether the given amount is numerically equal to this value <b>and</b> the units match.
	 */
	default boolean containsOrEquals(Amount amount) {
		return containsOrEquals(amount, BigDecimal.ZERO);
	}

	/**
	 * If this is a range, returns whether the given amount lies within the range <b>and</b> the units match. In this
	 * case, the delta is ignored. If this is a plain value, returns whether the given amount is numerically at
	 * most delta away from to this value <b>and</b> the units match.
	 */
	boolean containsOrEquals(Amount amount, BigDecimal delta);

	/**
	 * Returns whether this Amount or Range can be considered equal to the given other. This is the case if both are
	 * equal ranges, both are equal numbers, or one is a range that contains the other number.
	 */
	default boolean equalTo(AmountOrRange other) {
		return equalsWithRelativeDelta(other, BigDecimal.ZERO);
	}

	/**
	 * Returns whether this Amount or Range can be considered equal to the given other within a given RELATIVE delta.
	 * So if relativeDelta is 0.5, the amount to compare to can be off by up to 50% compared to this instance.
	 * This instance is used as base of the delta, except if this instance is an Amount and the other one is a range.
	 */
	boolean equalsWithRelativeDelta(AmountOrRange other, BigDecimal relativeDelta);

	AmountOrRange multiply(Amount other);
	AmountOrRange divide(Amount other);

	/**
	 * Copies this object, replacing the unit.
	 */
	AmountOrRange setUnit(Unit unit);

}
