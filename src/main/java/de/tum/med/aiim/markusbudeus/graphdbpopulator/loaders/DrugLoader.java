package de.tum.med.aiim.markusbudeus.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.io.IOException;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.*;
import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.PRODUCT_CONTAINS_DRUG_LABEL;

/**
 * This loader creates Drug nodes using the ITEM table in the MMI database and connects them to the Product nodes.
 * Requires product nodes to already exist.
 */
public class DrugLoader extends Loader {

	private static final String ID = "ID";
	private static final String PRODUCT_ID = "PRODUCTID";

	public DrugLoader(Session session) throws IOException {
		super("ITEM.CSV", session);
	}

	@Override
	protected void executeLoad() {
		executeQuery(
				"CREATE CONSTRAINT drugMmiIdConstraint IF NOT EXISTS FOR (d:" + DRUG_LABEL + ") REQUIRE d.mmiId IS UNIQUE"
		);

		executeQuery(withLoadStatement(
				"CREATE (d:" + DRUG_LABEL + " {mmiId: " + intRow(ID) + "})"
		));

		executeQuery(withLoadStatement(
				"MATCH (d:" + DRUG_LABEL + " {mmiId: " + intRow(ID) + "}) " +
						"MATCH (p:" + PRODUCT_LABEL + " {mmiId: " + intRow(PRODUCT_ID) + "}) " +
						"WITH d, p " +
						"CREATE (p)-[r:" + PRODUCT_CONTAINS_DRUG_LABEL + "]->(d)"
		));
	}
}
