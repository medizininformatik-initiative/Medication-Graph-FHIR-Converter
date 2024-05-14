package de.medizininformatikinitiative.medgraph.searchengine.db;

import org.jetbrains.annotations.NotNull;
import org.neo4j.driver.Value;

import java.math.BigDecimal;

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
	@NotNull
	public final String unit;

	DbAmount(Value value) {
		unit = value.get("unit", (String) null);
		amount = fromString(value.get("amount", (String) null));
	}

}
