package de.medizininformatikinitiative.medgraph.searchengine.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * An amount with or without a unit.
 *
 * @author Markus Budeus
 */
public class RawAmount {

	@NotNull
	public final BigDecimal number;

	public RawAmount(@NotNull BigDecimal number) {
		this.number = number;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RawAmount amount = (RawAmount) o;
		return Objects.equals(number, amount.number);
	}

	@Override
	public int hashCode() {
		return Objects.hash(number);
	}

	@Override
	public String toString() {
		return number.toString();
	}

}
