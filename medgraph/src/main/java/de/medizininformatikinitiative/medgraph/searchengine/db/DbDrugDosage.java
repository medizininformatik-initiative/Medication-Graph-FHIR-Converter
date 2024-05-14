package de.medizininformatikinitiative.medgraph.searchengine.db;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.neo4j.driver.Value;

import java.util.List;

/**
 * The amount of a drug and dosages of active ingredients as given in the database.
 *
 * @author Markus Budeus
 */
public class DbDrugDosage {

	@Nullable
	public final DbAmount amount;
	@NotNull
	public final List<DbDosage> dosages;

	DbDrugDosage(Value value) {
		dosages = value.get("dosage").asList(DbDosage::new);
		Value amount = value.get("amount");
		if (amount.isNull()) {
			this.amount = null;
		} else {
			this.amount = new DbAmount(amount);
		}
	}

}
