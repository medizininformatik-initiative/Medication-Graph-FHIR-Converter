package de.tum.med.aiim.markusbudeus.matcher.model;

import java.math.BigDecimal;
import java.util.Objects;

public class AmountRange extends Amount {

	private final BigDecimal to;

	public AmountRange(BigDecimal from, BigDecimal to, String unit) {
		super(from, unit);
		this.to = to;
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
		if (to == null) return super.toString();
		return number + "-" + to + (unit != null ? unit : "");
	}
}
