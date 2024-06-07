package de.medizininformatikinitiative.medgraph.searchengine.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * An amount range, i.e. an amount which is between two different values.
 *
 * @author Markus Budeus
 */
public final class AmountRange implements AmountOrRange {

	/**
	 * Creates a new {@link Amount} or {@link AmountRange} depending on whether an upper range end is specified or not.
	 *
	 * @param fromOrNumber the exact number of the amount or the lower end of the amount range
	 * @param toOrNull     the upper end of the amount range or null if an exact amount is to be specified
	 * @param unit         the unit of the amount
	 * @return if toOrNull is null, a corresponding {@link Amount}-instance, otherwise an {@link AmountRange}
	 */
	public static AmountOrRange ofNullableUpperEnd(@NotNull BigDecimal fromOrNumber, @Nullable BigDecimal toOrNull,
	                                               @Nullable String unit) {
		if (toOrNull == null) return new Amount(fromOrNumber, unit);
		return new AmountRange(fromOrNumber, toOrNull, unit);
	}

	/**
	 * The lower end of the amount range.
	 */
	@NotNull
	private final BigDecimal from;
	/**
	 * The upper end of the amount range.
	 */
	@NotNull
	private final BigDecimal to;

	/**
	 * The unit of this amount range or null if no unit is specified.
	 */
	@Nullable
	private final String unit;

	public AmountRange(@NotNull BigDecimal from, @NotNull BigDecimal to, @Nullable String unit) {
		this.from = from;
		this.to = to;
		if (to.compareTo(from) < 0) {
			throw new IllegalArgumentException(
					"The range start (" + from + ") must be less than the range end (" + to + ")");
		}
		this.unit = unit;
	}

	/**
	 * Returns the upper end of the amount range.
	 */
	@NotNull
	public BigDecimal getTo() {
		return to;
	}

	/**
	 * Returns the lower end of the amount range.
	 */
	@NotNull
	public BigDecimal getFrom() {
		return from;
	}

	@Nullable
	public String getUnit() {
		return unit;
	}

	@Override
	public boolean containsOrEquals(Amount amount, BigDecimal delta) {
		if (!Objects.equals(getUnit(), amount.getUnit())) return false;
		BigDecimal number = amount.getNumber();
		return from.compareTo(number) <= 0 && to.compareTo(number) >= 0;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		AmountRange that = (AmountRange) object;
		return Objects.equals(from, that.from) && Objects.equals(to,
				that.to) && Objects.equals(unit, that.unit);
	}

	@Override
	public int hashCode() {
		return Objects.hash(from, to, unit);
	}

	@Override
	public String toString() {
		return from + "-" + to + (getUnit() != null ? getUnit() : "");
	}
}
