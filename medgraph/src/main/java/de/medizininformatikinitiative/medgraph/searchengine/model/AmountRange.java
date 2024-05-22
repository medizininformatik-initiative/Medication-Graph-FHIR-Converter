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
public class AmountRange extends Amount {

	/**
	 * Creates a new {@link Amount} or {@link AmountRange} depending on whether an upper range end is specified or not.
	 *
	 * @param fromOrNumber the exact number of the amount or the lower end of the amount range
	 * @param toOrNull     the upper end of the amount range or null if an exact amount is to be specified
	 * @param unit         the unit of the amount
	 * @return if toOrNull is null, a corresponding {@link Amount}-instance, otherwise an {@link AmountRange}
	 */
	public static Amount ofNullableUpperEnd(@NotNull BigDecimal fromOrNumber, @Nullable BigDecimal toOrNull,
	                                        @Nullable String unit) {
		if (toOrNull == null) return new Amount(fromOrNumber, unit);
		return new AmountRange(fromOrNumber, toOrNull, unit);
	}

	/**
	 * The upper end of the amount range.
	 */
	@NotNull
	private final BigDecimal to;

	public AmountRange(@NotNull BigDecimal from, @NotNull BigDecimal to, @Nullable String unit) {
		super(from, unit);
		this.to = to;
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
		return getNumber();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		AmountRange that = (AmountRange) o;
		return Objects.equals(to, that.to);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), to);
	}

	@Override
	public String toString() {
		return getNumber() + "-" + to + (getUnit() != null ? getUnit() : "");
	}
}
