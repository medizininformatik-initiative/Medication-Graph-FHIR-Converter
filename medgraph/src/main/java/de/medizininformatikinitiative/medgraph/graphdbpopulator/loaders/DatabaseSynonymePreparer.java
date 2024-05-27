package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions;
import org.neo4j.driver.Session;

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
		startSubtask("Create synonyme nodes for GSRS names");
		addGsrsNamesAsSynonymes();
		startSubtask("Create synonyme nodes for INNs");
		addInnAsSynonymes();
	}

	void removeHtmlSynonymes() {
		executeQuery("MATCH (sy:" + DatabaseDefinitions.SYNONYME_LABEL + ") " +
				"WHERE sy.name CONTAINS '</' " +
				"DETACH DELETE sy");
	}

	void addProductNamesAsSynonymes() {
		executeQuery(
				"MATCH (p:" + DatabaseDefinitions.PRODUCT_LABEL + ") " +
						withRowLimit("WITH p MERGE (sy:" + DatabaseDefinitions.SYNONYME_LABEL + " {name: p.name}) " +
								"MERGE (sy)-[:" + DatabaseDefinitions.SYNONYME_REFERENCES_NODE_LABEL + "]->(p)")
		);
	}

	void addSubstanceNamesAsSynonymes() {
		executeQuery(
				"MATCH (s:" + DatabaseDefinitions.SUBSTANCE_LABEL + ") " +
						withRowLimit("WITH s MERGE (sy:" + DatabaseDefinitions.SYNONYME_LABEL + " {name: s.name}) " +
								"MERGE (sy)-[:" + DatabaseDefinitions.SYNONYME_REFERENCES_NODE_LABEL + "]->(s)")
		);
	}

	void addGsrsNamesAsSynonymes() {
		executeQuery(
				"MATCH (g:" + DatabaseDefinitions.UNII_LABEL + ")-[:" + DatabaseDefinitions.CODE_REFERENCE_RELATIONSHIP_NAME + "]->(s:" + DatabaseDefinitions.SUBSTANCE_LABEL + ") " +
						withRowLimit(
								"WITH g, s MERGE (sy:" + DatabaseDefinitions.SYNONYME_LABEL + " {name: g.gsrsName}) " +
										"MERGE (sy)-[:" + DatabaseDefinitions.SYNONYME_REFERENCES_NODE_LABEL + "]->(s)")
		);
	}

	void addInnAsSynonymes() {
		executeQuery(
				"MATCH (i:" + DatabaseDefinitions.INN_LABEL + ")-[:" + DatabaseDefinitions.CODE_REFERENCE_RELATIONSHIP_NAME + "]->(s:" + DatabaseDefinitions.SUBSTANCE_LABEL + ") " +
						withRowLimit("WITH i, s MERGE (sy:" + DatabaseDefinitions.SYNONYME_LABEL + " {name: i.code}) " +
								"MERGE (sy)-[:" + DatabaseDefinitions.SYNONYME_REFERENCES_NODE_LABEL + "]->(s)")
		);
	}

	void makeAllSynonymesLowerCase() {
		executeQuery(
				"MATCH (sy:" + DatabaseDefinitions.SYNONYME_LABEL + ") " +
						withRowLimit(
								"WITH sy MERGE (sy2:" + DatabaseDefinitions.SYNONYME_LABEL + " {name: toLower(sy.name)}) " +
										"WITH sy, sy2 " +
										"MATCH (sy)-[r:" + DatabaseDefinitions.SYNONYME_REFERENCES_NODE_LABEL + "]->(t) " +
										"MERGE (sy2)-[:" + DatabaseDefinitions.SYNONYME_REFERENCES_NODE_LABEL + "]->(t) ")
		);
		executeQuery(
				"MATCH (sy:" + DatabaseDefinitions.SYNONYME_LABEL + ") " +
						"WHERE sy.name <> toLower(sy.name) " +
						withRowLimit("WITH sy DETACH DELETE sy")
		);
	}

}
