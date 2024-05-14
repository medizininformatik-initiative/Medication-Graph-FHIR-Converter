package de.medizininformatikinitiative.medgraph.searchengine.db;

import java.util.Set;

/**
 * Interface which exposes typical queries which are sent against the database, to keep users decoupled from the
 * Neo4j driver.
 *
 * @author Markus Budeus
 */
public interface Database {

	// TODO: A Cache Proxy implementation of this interface could be made, might reduce performance overhead from DB reads

	/**
	 * Retrieves drug dosage information for the products with the given ids.
	 *
	 * @param productIds the product ids for which to retrieve the information from the database.
	 * @return a set of {@link DbDosagesByProduct}-instances containing the requested data for each product which could
	 * be found
	 */
	Set<DbDosagesByProduct> getDrugDosagesByProduct(Set<Long> productIds);

}
