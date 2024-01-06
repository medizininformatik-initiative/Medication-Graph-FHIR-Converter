package de.tum.med.aiim.markusbudeus.graphdbpopulator.loaders;

import de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseConnection;
import org.neo4j.driver.Session;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.*;

/**
 * This class improves the synonyme nodes in the graph database.
 */
public class DatabaseSynonymePreparer extends Loader {

	public static void main(String[] args) {
		DatabaseConnection.runSession(session -> {
			DatabaseSynonymePreparer preparer = new DatabaseSynonymePreparer(session);
			preparer.executeLoad();
		});
	}

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
		executeQuery("MATCH (sy:" + SYNONYME_LABEL + ") " +
				"WHERE sy.name CONTAINS '</' " +
				"DETACH DELETE sy");
	}

	void addProductNamesAsSynonymes() {
		executeQuery(
				"MATCH (p:" + PRODUCT_LABEL + ") " +
						"MERGE (sy:" + SYNONYME_LABEL + " {name: p.name}) " +
						"MERGE (sy)-[:" + SYNONYME_REFERENCES_NODE_LABEL + "]->(p)"
		);
	}

	void addSubstanceNamesAsSynonymes() {
		executeQuery(
				"MATCH (s:" + SUBSTANCE_LABEL + ") " +
						"MERGE (sy:" + SYNONYME_LABEL + " {name: s.name}) " +
						"MERGE (sy)-[:" + SYNONYME_REFERENCES_NODE_LABEL + "]->(s)"
		);
	}

	void addGsrsNamesAsSynonymes() {
		executeQuery(
				"MATCH (g:" + UNII_LABEL + ")-[:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]->(s:" + SUBSTANCE_LABEL + ") " +
						"MERGE (sy:" + SYNONYME_LABEL + " {name: g.gsrsName}) " +
						"MERGE (sy)-[:" + SYNONYME_REFERENCES_NODE_LABEL + "]->(s)"
		);
	}

	void addInnAsSynonymes() {
		executeQuery(
				"MATCH (i:" + INN_LABEL + ")-[:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]->(s:" + SUBSTANCE_LABEL + ") " +
						"MERGE (sy:" + SYNONYME_LABEL + " {name: i.code}) " +
						"MERGE (sy)-[:" + SYNONYME_REFERENCES_NODE_LABEL + "]->(s)"
		);
	}

	void makeAllSynonymesLowerCase() {
		executeQuery(
				"MATCH (sy:" + SYNONYME_LABEL + ") " +
						"MERGE (sy2:" + SYNONYME_LABEL + " {name: toLower(sy.name)}) " +
						"WITH sy, sy2 " +
						"MATCH (sy)-[r:" + SYNONYME_REFERENCES_NODE_LABEL + "]->(t) " +
						"MERGE (sy2)-[:" + SYNONYME_REFERENCES_NODE_LABEL + "]->(t) "
		);
		executeQuery(
				"MATCH (sy:" + SYNONYME_LABEL + ") " +
						"WHERE sy.name <> toLower(sy.name) " +
						"DETACH DELETE sy"
		);
	}

}
