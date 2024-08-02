package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;

/**
 * This class improves the synonym nodes in the graph database by removing HTML synonyms (which make no sense to use)
 * and adding synonym nodes for names already known elsewhere in the knowledge graph.
 *
 * @author Markus Budeus
 */
public class DatabaseSynonymPreparer extends Loader {

	public DatabaseSynonymPreparer(Session session) {
		super(session);
	}

	@Override
	protected void executeLoad() {
		startSubtask("Remove synonyms with HTML content");
		removeHtmlSynonyms();
		startSubtask("Create synonym nodes for product names");
		addProductNamesAsSynonyms();
		startSubtask("Create synonym nodes for substance names");
		addSubstanceNamesAsSynonyms();
		startSubtask("Create synonym nodes for EDQM Standard Terms concept names");
		addEdqmConceptNamesAsSynonyms();
		startSubtask("Create synonym nodes for GSRS names");
		addGsrsNamesAsSynonyms();
		startSubtask("Create synonym nodes for INNs");
		addInnAsSynonyms();
	}

	void removeHtmlSynonyms() {
		executeQuery("MATCH (sy:" + SYNONYM_LABEL + ") " +
				"WHERE sy.name CONTAINS '</' " +
				"DETACH DELETE sy");
	}

	void addProductNamesAsSynonyms() {
		executeQuery(
				"MATCH (p:" + PRODUCT_LABEL + ") " +
						withRowLimit("WITH p MERGE (sy:" + SYNONYM_LABEL + " {name: p.name}) " +
								"MERGE (sy)-[:" + SYNONYM_REFERENCES_NODE_LABEL + "]->(p)")
		);
	}

	void addSubstanceNamesAsSynonyms() {
		executeQuery(
				"MATCH (s:" + SUBSTANCE_LABEL + ") " +
						withRowLimit("WITH s MERGE (sy:" + SYNONYM_LABEL + " {name: s.name}) " +
								"MERGE (sy)-[:" + SYNONYM_REFERENCES_NODE_LABEL + "]->(s)")
		);
	}

	void addEdqmConceptNamesAsSynonyms() {
		executeQuery(
				"MATCH (e:" + EDQM_LABEL + ") " +
						withRowLimit("WITH e MERGE (sy:" + SYNONYM_LABEL + " {name: e.name}) " +
								"MERGE (sy)-[:" + SYNONYM_REFERENCES_NODE_LABEL + "]->(e)")
		);
	}

	void addGsrsNamesAsSynonyms() {
		executeQuery(
				"MATCH (g:" + UNII_LABEL + ")-[:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]->(s:" + SUBSTANCE_LABEL + ") " +
						withRowLimit(
								"WITH g, s MERGE (sy:" + SYNONYM_LABEL + " {name: g.gsrsName}) " +
										"MERGE (sy)-[:" + SYNONYM_REFERENCES_NODE_LABEL + "]->(s)")
		);
	}

	void addInnAsSynonyms() {
		executeQuery(
				"MATCH (i:" + INN_LABEL + ")-[:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]->(s:" + SUBSTANCE_LABEL + ") " +
						withRowLimit("WITH i, s MERGE (sy:" + SYNONYM_LABEL + " {name: i.code}) " +
								"MERGE (sy)-[:" + SYNONYM_REFERENCES_NODE_LABEL + "]->(s)")
		);
	}

	void makeAllSynonymsLowerCase() {
		executeQuery(
				"MATCH (sy:" + SYNONYM_LABEL + ") " +
						withRowLimit(
								"WITH sy MERGE (sy2:" + SYNONYM_LABEL + " {name: toLower(sy.name)}) " +
										"WITH sy, sy2 " +
										"MATCH (sy)-[r:" + SYNONYM_REFERENCES_NODE_LABEL + "]->(t) " +
										"MERGE (sy2)-[:" + SYNONYM_REFERENCES_NODE_LABEL + "]->(t) ")
		);
		executeQuery(
				"MATCH (sy:" + SYNONYM_LABEL + ") " +
						"WHERE sy.name <> toLower(sy.name) " +
						withRowLimit("WITH sy DETACH DELETE sy")
		);
	}

}
