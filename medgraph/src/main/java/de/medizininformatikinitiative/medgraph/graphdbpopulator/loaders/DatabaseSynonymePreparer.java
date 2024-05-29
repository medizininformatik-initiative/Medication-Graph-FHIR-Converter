package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;

/**
 * This class improves the synonyme nodes in the graph database by removing HTML synonymes (which make no sense to use)
 * and adding synonyme nodes for names already known elsewhere in the knowledge graph.
 *
 * @author Markus Budeus
 */
public class DatabaseSynonymePreparer extends Loader {

	public DatabaseSynonymePreparer(Session session) {
		super(session);
	}

	@Override
	protected void executeLoad() {
		startSubtask("Remove synonymes with HTML content");
		removeHtmlSynonymes();
		startSubtask("Create synonyme nodes for product names");
		addProductNamesAsSynonymes();
		startSubtask("Create synonyme nodes for substance names");
		addSubstanceNamesAsSynonymes();
		startSubtask("Create synonyme nodes for EDQM Standard Terms concept names");
		addEdqmConceptNamesAsSynonymes();
		startSubtask("Create synonyme nodes for GSRS names");
		addGsrsNamesAsSynonymes();
		startSubtask("Create synonyme nodes for INNs");
		addInnAsSynonymes();
	}

	void removeHtmlSynonymes() {
		executeQuery("MATCH (sy:" + SYNONYME_LABEL + ") " +
				"WHERE sy.name CONTAINS '</' " +
				"DETACH DELETE sy");
	}

	void addProductNamesAsSynonymes() {
		executeQuery(
				"MATCH (p:" + PRODUCT_LABEL + ") " +
						withRowLimit("WITH p MERGE (sy:" + SYNONYME_LABEL + " {name: p.name}) " +
								"MERGE (sy)-[:" + SYNONYME_REFERENCES_NODE_LABEL + "]->(p)")
		);
	}

	void addSubstanceNamesAsSynonymes() {
		executeQuery(
				"MATCH (s:" + SUBSTANCE_LABEL + ") " +
						withRowLimit("WITH s MERGE (sy:" + SYNONYME_LABEL + " {name: s.name}) " +
								"MERGE (sy)-[:" + SYNONYME_REFERENCES_NODE_LABEL + "]->(s)")
		);
	}

	void addEdqmConceptNamesAsSynonymes() {
		executeQuery(
				"MATCH (e:" + EDQM_LABEL + ") " +
						withRowLimit("WITH e MERGE (sy:" + SYNONYME_LABEL + " {name: e.name}) " +
								"MERGE (sy)-[:" + SYNONYME_REFERENCES_NODE_LABEL + "]->(e)")
		);
	}

	void addGsrsNamesAsSynonymes() {
		executeQuery(
				"MATCH (g:" + UNII_LABEL + ")-[:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]->(s:" + SUBSTANCE_LABEL + ") " +
						withRowLimit(
								"WITH g, s MERGE (sy:" + SYNONYME_LABEL + " {name: g.gsrsName}) " +
										"MERGE (sy)-[:" + SYNONYME_REFERENCES_NODE_LABEL + "]->(s)")
		);
	}

	void addInnAsSynonymes() {
		executeQuery(
				"MATCH (i:" + INN_LABEL + ")-[:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]->(s:" + SUBSTANCE_LABEL + ") " +
						withRowLimit("WITH i, s MERGE (sy:" + SYNONYME_LABEL + " {name: i.code}) " +
								"MERGE (sy)-[:" + SYNONYME_REFERENCES_NODE_LABEL + "]->(s)")
		);
	}

	void makeAllSynonymesLowerCase() {
		executeQuery(
				"MATCH (sy:" + SYNONYME_LABEL + ") " +
						withRowLimit(
								"WITH sy MERGE (sy2:" + SYNONYME_LABEL + " {name: toLower(sy.name)}) " +
										"WITH sy, sy2 " +
										"MATCH (sy)-[r:" + SYNONYME_REFERENCES_NODE_LABEL + "]->(t) " +
										"MERGE (sy2)-[:" + SYNONYME_REFERENCES_NODE_LABEL + "]->(t) ")
		);
		executeQuery(
				"MATCH (sy:" + SYNONYME_LABEL + ") " +
						"WHERE sy.name <> toLower(sy.name) " +
						withRowLimit("WITH sy DETACH DELETE sy")
		);
	}

}
