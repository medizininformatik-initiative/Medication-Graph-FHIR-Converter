package de.medizininformatikinitiative.medgraph.searchengine.db;

import org.jetbrains.annotations.NotNull;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.MapAccessor;

import java.util.List;
import java.util.Objects;

/**
 * Contains dosage information about all drugs which belong to this product.
 *
 * @author Markus Budeus
 */
public class DbDosagesByProduct {

	/**
	 * The MMI id of the product.
	 */
	public final long productId;
	/**
	 * The dosage information for each drug linked to this product.
	 */
	@NotNull
	public final List<DbDrugDosage> drugDosages;

	DbDosagesByProduct(MapAccessor value) {
		productId = value.get("productId").asLong();
		drugDosages = value.get("drugDosages").asList(DbDrugDosage::new);
	}

	public DbDosagesByProduct(long productId, @NotNull List<DbDrugDosage> drugDosages) {
		this.productId = productId;
		this.drugDosages = drugDosages;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DbDosagesByProduct that = (DbDosagesByProduct) o;
		return productId == that.productId && Objects.equals(drugDosages, that.drugDosages);
	}

	@Override
	public int hashCode() {
		return Objects.hash(productId, drugDosages);
	}

	@Override
	public String toString() {
		return "DbDosagesByProduct{" +
				"productId=" + productId +
				", drugDosages=" + drugDosages +
				'}';
	}
}
