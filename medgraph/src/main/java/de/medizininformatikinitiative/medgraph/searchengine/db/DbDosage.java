package de.medizininformatikinitiative.medgraph.searchengine.db;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.neo4j.driver.Value;

import java.math.BigDecimal;
import java.util.Objects;

import static de.medizininformatikinitiative.medgraph.searchengine.db.Tools.fromString;

/**
 * Represents a dosage (amount of an ingredient) as taken from the database. It is either an exact value or a range,
 * with a unit given in UCUM case-sensitive notation.
 *
 * @author Markus Budeus
 */
public class DbDosage {

	/**
	 * The minimum amount or, if {@link #amountTo} is null, the exact amount. (Given as massFrom in the database).
	 */
	@NotNull
	public final BigDecimal amountFrom;
	/**
	 * The maximum amount or null if {@link #amountFrom} is the exact amount.
	 */
	@Nullable
	public final BigDecimal amountTo;
	/**
	 * The unit of this dosage.
	 */
	@NotNull
	public final String unit;

	DbDosage(Value value) {
		unit = value.get("unit", (String) null);
		amountFrom = fromString(value.get("amountFrom", (String) null));
		amountTo = fromString(value.get("amountTo", (String) null));
	}

	public DbDosage(@NotNull BigDecimal amountFrom, @NotNull String unit) {
		this(amountFrom, null, unit);
	}

	public DbDosage(@NotNull BigDecimal amountFrom, @Nullable BigDecimal amountTo, @NotNull String unit) {
		this.amountFrom = amountFrom;
		this.amountTo = amountTo;
		this.unit = unit;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DbDosage dosage = (DbDosage) o;
		return Objects.equals(amountFrom, dosage.amountFrom) && Objects.equals(amountTo,
				dosage.amountTo) && Objects.equals(unit, dosage.unit);
	}

	@Override
	public int hashCode() {
		return Objects.hash(amountFrom, amountTo, unit);
	}

	@Override
	public String toString() {
		return amountFrom + (amountTo != null ? "-" + amountTo : "") + " " + unit;
	}

}
