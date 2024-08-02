package de.medizininformatikinitiative.medgraph.searchengine.provider;

import de.medizininformatikinitiative.medgraph.common.EDQM;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.MapAccessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;

/**
 * @author Markus Budeus
 */
class DatabaseProviders {

	/**
	 * Returns a stream of mapped identifiers where the identifier is a known synonym for a product in the database and
	 * the {@link Identifiable} is the corresponding product.
	 *
	 * @param session the session to access the database with
	 */
	public static Stream<MappedIdentifier<String>> getProductSynonyms(Session session) {
		return downloadMmiObjectSynonyms(session, PRODUCT_LABEL)
				.map(DatabaseProviders::toProduct);
	}

	/**
	 * Returns a stream of mapped identifiers where the identifier is a known synonym for a substance in the database
	 * and the {@link Identifiable} is the corresponding substance.
	 *
	 * @param session the session to access the database with
	 */
	public static Stream<MappedIdentifier<String>> getSubstanceSynonyms(Session session) {
		return downloadMmiObjectSynonyms(session, SUBSTANCE_LABEL)
				.map(DatabaseProviders::toSubstance);
	}

	/**
	 * Returns a stream of mapped identifiers where the identifier is a known synonym for an EDQM Standard Terms concept
	 * and the {@link Identifiable} object is an {@link EdqmConcept} or, if applicable, a
	 * {@link EdqmPharmaceuticalDoseForm}.
	 *
	 * @param session the session to access the database with
	 */
	public static Stream<MappedIdentifier<String>> getEdqmConceptIdentifiers(Session session) {
		List<Record> records = downloadEdqmConceptData(session).toList();
		List<MappedIdentifier<String>> identifiers = downloadEdqmConceptData(session).flatMap(record -> toEdqmConcepts(record).stream()).toList();

		return downloadEdqmConceptData(session).flatMap(record -> toEdqmConcepts(record).stream());
	}

	/**
	 * Queries synonyms pointing to nodes with the given label in the database. Do not pass a user-provided string as
	 * label, as it is inserted into the query unsafely. This is because Neo4j does not support labels being
	 * parameterized.
	 *
	 * @param session the session to use for accessing the database
	 * @param label   the label on the nodes which shall be referenced by the synonyms - DO NOT PASS IN USER-PROVIDED
	 *                STRINGS
	 * @return a stream of found records, each record containing the synonym name, mmiId of target and name of target
	 */
	private static Stream<Record> downloadMmiObjectSynonyms(Session session, String label) {
		return session.run(
				"MATCH (sy:" + SYNONYM_LABEL + ")-[:"+ SYNONYM_REFERENCES_NODE_LABEL +"]->(t:" + label + ") " +
						"RETURN sy.name, t.mmiId, t.name"
		).stream();
	}

	private static Stream<Record> downloadEdqmConceptData(Session session) {
		return session.run(
				"MATCH (e:" + EDQM_LABEL + ") " +
						"OPTIONAL MATCH (e)-[:" + EDQM_HAS_CHARACTERISTIC_LABEL + "]->(c:" + EDQM_LABEL + ") " +
						"WITH e, collect(CASE WHEN c IS NULL THEN NULL ELSE {code:c.code,name:c.name,type:c.type} END) as characteristics " +
						"OPTIONAL MATCH (s:" + SYNONYM_LABEL + ")-[:" + SYNONYM_REFERENCES_NODE_LABEL + "]->(e) " +
						"RETURN e.code AS code, e.name AS name, e.type AS type, characteristics, " +
						"collect(DISTINCT s.name) AS identifiers"
		).stream();
	}

	private static MappedIdentifier<String> toProduct(Record record) {
		return toIdentifiable(record, Product::new);
	}

	private static MappedIdentifier<String> toSubstance(Record record) {
		return toIdentifiable(record, Substance::new);
	}

	private static List<MappedIdentifier<String>> toEdqmConcepts(Record record) {
		EdqmConcept concept = toEdqmConcept(record);
		List<String> identifiers = record.get("identifiers").asList(Value::asString, Collections.emptyList());
		if (identifiers.isEmpty()) {
			System.err.println("Warning: EDQM Concept "+concept.getCode()+" seems to have no identifiers!");
		}
		List<MappedIdentifier<String>> result = new ArrayList<>();
		identifiers.forEach(identifier -> result.add(new MappedIdentifier<>(identifier, concept)));
		return result;
	}

	private static EdqmConcept toEdqmConcept(MapAccessor mapAccessor) {
		String code = mapAccessor.get("code").asString();
		String name = mapAccessor.get("name").asString();
		String type = mapAccessor.get("type").asString();
		EDQM conceptClass = EDQM.fromTypeFullName(type);
		if (conceptClass == null) throw new IllegalStateException("Unexpected concept type: " + type);

		if (conceptClass != EDQM.PHARMACEUTICAL_DOSE_FORM) {
			return new EdqmConcept(code, name, conceptClass);
		}

		List<EdqmConcept> linkedConcepts = mapAccessor.get("characteristics").asList(DatabaseProviders::toEdqmConcept,
				Collections.emptyList());

		return new EdqmPharmaceuticalDoseForm(code, name, linkedConcepts);
	}

	private static MappedIdentifier<String> toIdentifiable(Record record,
	                                                       BiFunction<Long, String, Identifiable> intantiatorFunction) {
		long mmiId = record.get(1).asLong();
		String name = record.get(2).asString();
		Identifiable target = intantiatorFunction.apply(mmiId, name);
		return new MappedIdentifier<>(record.get(0).asString(), target);
	}

}
