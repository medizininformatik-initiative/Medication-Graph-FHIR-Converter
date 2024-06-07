package de.medizininformatikinitiative.medgraph.searchengine.db;

import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.DetailedProduct;

import java.util.Collection;
import java.util.Set;

/**
 * Interface which exposes typical queries which are sent against the database, to keep users decoupled from the Neo4j
 * driver.
 *
 * @author Markus Budeus
 */
public interface Database {

	// TODO: A Cache Proxy implementation of this interface could be made, might reduce performance overhead from DB reads

	/**
	 * Acquires detailed information about the products with the given product ids. Ids which cannot be resolved into a
	 * product are discarded.
	 *
	 * @param productIds the ids of the products for which to retrieve detailed information
	 * @return a set of {@link DetailedProduct}-instances, each holding information about a product which could be
	 * resolved
	 */
	Set<DetailedProduct> getDetailedProductInfo(Collection<Long> productIds);

}
