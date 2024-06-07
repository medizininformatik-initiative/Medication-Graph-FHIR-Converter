package de.medizininformatikinitiative.medgraph.searchengine.model;

import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Describes an amount of something, or possibly an amount range.
 *
 * @author Markus Budeus
 */
public sealed interface AmountOrRange permits Amount, AmountRange {

	/**
	 * The unit of this amount specification or null if no unit is specified.
	 */
	@Nullable
	String getUnit();

	/**
	 * If this is a range, returns whether the given amount lies within the range <b>and</b> the units match. If this is
	 * a plain value, returns whether the given amount is numerically equal to this value <b>and</b> the units match.
	 */
	default boolean containsOrEquals(Amount amount) {
		return containsOrEquals(amount, BigDecimal.ZERO);
	}

	/**
	 * If this is a range, returns whether the given amount lies within the range <b>and</b> the units match. In this
	 * case, the delta is ignored. If this is a plain value, returns whether the given amount is is numerically at
	 * most delta away from to this value <b>and</b> the units match.
	 */
	boolean containsOrEquals(Amount amount, BigDecimal delta);

	// TODO Keep?
//	/**
//	 * Scales this amount or range to the given accuracy. It's like calling
//	 * {@link BigDecimal#setScale(int, RoundingMode)}. In case this is a plain amount, its value is scaled. If this is a
//	 * range, both the lower and upper bound are set to this scale.
//	 *
//	 * @param scale        the scale to apply
//	 * @param roundingMode the rounding mode to apply
//	 * @return the newly created {@link AmountRange} with the given scaling applied
//	 * @see BigDecimal#setScale(int, RoundingMode)
//	 */
//	AmountOrRange setScale(int scale, RoundingMode roundingMode);

}
