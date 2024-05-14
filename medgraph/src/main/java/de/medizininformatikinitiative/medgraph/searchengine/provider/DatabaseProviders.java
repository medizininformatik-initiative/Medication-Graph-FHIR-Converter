package de.medizininformatikinitiative.medgraph.searchengine.provider;

import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Identifiable;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Product;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Substance;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;

import java.util.function.BiFunction;
import java.util.stream.Stream;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;
import static org.neo4j.driver.Values.parameters;

/**
 * @author Markus Budeus
 */
class DatabaseProviders {

	/**
	 * Returns a stream of mapped identifiers where the identifier is a known synonyme for a product in the database and
	 * the {@link Identifiable} is the corresponding product.
	 *
	 * @param session the session to access the database with
	 */
	public static Stream<MappedIdentifier<String>> getProductSynonymes(Session session) {
		return downloadMmiObjectSynonymes(session, PRODUCT_LABEL)
				.map(DatabaseProviders::toProduct);
	}

	/**
	 * Returns a stream of mapped identifiers where the identifier is a known synonyme for a substance in the database
	 * and the {@link Identifiable} is the corresponding substance.
	 *
	 * @param session the session to access the database with
	 */
	public static Stream<MappedIdentifier<String>> getSubstanceSynonymes(Session session) {
		return downloadMmiObjectSynonymes(session, SUBSTANCE_LABEL)
				.map(DatabaseProviders::toSubstance);
	}

	/**
	 * Queries synonymes pointing to nodes with the given label in the database.
	 *
	 * @param session the session to use for accessing the database
	 * @param label   the label on the nodes which shall be referenced by the synonymes
	 * @return a stream of found records, each record containing the synonyme name, mmiId of target and name of target
	 */
	private static Stream<Record> downloadMmiObjectSynonymes(Session session, String label) {
		return session.run(
				              new Query("MATCH (sy:" + SYNONYME_LABEL + ")--(t:$label) " +
						              "RETURN sy.name, t.mmiId, t.name",
						              parameters("label", label))
		              )
		              .stream();
	}

	private static MappedIdentifier<String> toProduct(Record record) {
		return toIdentifiable(record, Product::new);
	}

	private static MappedIdentifier<String> toSubstance(Record record) {
		return toIdentifiable(record, Substance::new);
	}

	private static MappedIdentifier<String> toIdentifiable(Record record,
	                                                       BiFunction<Long, String, Identifiable> intantiatorFunction) {
		long mmiId = record.get(1).asLong();
		String name = record.get(2).asString();
		Identifiable target = intantiatorFunction.apply(mmiId, name);
		return new MappedIdentifier<>(record.get(0).asString(), target);
	}

}
