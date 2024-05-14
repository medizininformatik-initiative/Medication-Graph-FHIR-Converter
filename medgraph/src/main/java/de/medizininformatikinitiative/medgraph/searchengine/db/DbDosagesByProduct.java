package de.medizininformatikinitiative.medgraph.searchengine.db;

import org.jetbrains.annotations.NotNull;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.MapAccessor;

import java.util.List;

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
}
