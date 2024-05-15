package de.medizininformatikinitiative.medgraph.searchengine.db;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.neo4j.driver.Value;

import java.math.BigDecimal;
import java.util.Objects;

import static de.medizininformatikinitiative.medgraph.searchengine.db.Tools.fromString;

/**
 * Represents an amount (e.g. amount of a drug) as taken from the database
 *
 * @author Markus Budeus
 */
public class DbAmount {

	/**
	 * The numeric value of the amount.
	 */
	@NotNull
	public final BigDecimal amount;
	/**
	 * The amount's unit.
	 */
	@Nullable
	public final String unit;

	DbAmount(Value value) {
		unit = value.get("unit", (String) null);
		amount = fromString(value.get("amount", (String) null));
	}

	public DbAmount(@NotNull BigDecimal amount, @Nullable String unit) {
		this.amount = amount;
		this.unit = unit;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DbAmount dbAmount = (DbAmount) o;
		return Objects.equals(amount, dbAmount.amount) && Objects.equals(unit, dbAmount.unit);
	}

	@Override
	public int hashCode() {
		return Objects.hash(amount, unit);
	}

	@Override
	public String toString() {
		return amount + (unit != null ? " " + unit : "");
	}
}
