package de.medizininformatikinitiative.medgraph.searchengine.model;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * An amount range, i.e. an amount which is between two different values.
 *
 * @author Markus Budeus
 */
public class AmountRange extends Amount {

	/**
	 * The upper end of the amount range.
	 */
	@NotNull
	private final BigDecimal to;

	public AmountRange(BigDecimal from, @NotNull BigDecimal to, String unit) {
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
		return number;
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
		return number + "-" + to + (unit != null ? unit : "");
	}
}
