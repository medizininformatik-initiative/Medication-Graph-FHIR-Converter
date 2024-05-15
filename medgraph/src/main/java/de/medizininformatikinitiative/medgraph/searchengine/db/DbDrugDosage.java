package de.medizininformatikinitiative.medgraph.searchengine.db;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.neo4j.driver.Value;

import java.util.List;
import java.util.Objects;

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

	public DbDrugDosage(@Nullable DbAmount amount, @NotNull List<DbDosage> dosages) {
		this.amount = amount;
		this.dosages = dosages;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DbDrugDosage that = (DbDrugDosage) o;
		return Objects.equals(amount, that.amount) && Objects.equals(dosages, that.dosages);
	}

	@Override
	public int hashCode() {
		return Objects.hash(amount, dosages);
	}

	@Override
	public String toString() {
		return "DbDrugDosage{" +
				"amount=" + amount +
				", dosages=" + dosages +
				'}';
	}
}
