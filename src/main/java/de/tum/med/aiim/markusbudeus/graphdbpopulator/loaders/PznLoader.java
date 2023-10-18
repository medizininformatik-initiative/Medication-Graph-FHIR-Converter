package de.tum.med.aiim.markusbudeus.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.io.IOException;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.*;

/**
 * This loader uses the PACKAGE table to create PZN nodes and make them point to the corresponding product nodes.
 */
public class PznLoader extends CsvLoader {

	private static final String PRODUCT_ID = "PRODUCTID";
	private static final String PZN = "PZN";

	public PznLoader(Session session) throws IOException {
		super("PACKAGE.CSV", session);
	}

	@Override
	protected void executeLoad() {
		executeQuery(
				"CREATE CONSTRAINT pznCodeConstraint IF NOT EXISTS FOR (p:" + PZN_LABEL + ") REQUIRE p.code IS UNIQUE"
		);

		executeQuery(withLoadStatement(
				"MERGE (p:" + PZN_LABEL + " {code: " + row(PZN) + "}) " +
						"ON CREATE SET p:" + CODE_LABEL
		));

		executeQuery(withLoadStatement(
				"MATCH (p:" + PZN_LABEL + " {code: " + row(PZN) + "}) " +
						"MATCH (i:" + PRODUCT_LABEL + " {mmiId: " + intRow(PRODUCT_ID) + "}) " +
						"MERGE (p)-[:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]->(i)"
		));
	}
}
