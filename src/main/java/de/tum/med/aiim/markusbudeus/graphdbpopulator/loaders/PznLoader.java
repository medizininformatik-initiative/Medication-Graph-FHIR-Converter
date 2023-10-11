package de.tum.med.aiim.markusbudeus.graphdbpopulator.loaders;

import org.neo4j.driver.Query;
import org.neo4j.driver.Session;

import java.io.IOException;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.*;

/**
 * This migrator uses the PACKAGE table to create PZN nodes and make them point to the corresponding product nodes.
 */
public class PznLoader extends Loader {

	private static final String PRODUCT_ID = "PRODUCTID";
	private static final String PZN = "PZN";

	public PznLoader(Session session) throws IOException {
		super("PACKAGE.CSV", session);
	}

	@Override
	protected void executeLoad() {
		session.run(new Query(
				"CREATE CONSTRAINT pznCodeConstraint IF NOT EXISTS FOR (p:" + PZN_LABEL + ") REQUIRE p.code IS UNIQUE"
		));

//		session.run(new Query(withLoadStatement(
//				"MERGE (p:" + PZN_LABEL + " {code: " + row(PZN) + "}) " +
//						"ON CREATE SET p:" + CODING_SYSTEM_LABEL + " " +
//						"WITH * " +
//						"MATCH (i:" + PRODUCT_LABEL + " {mmiId: " + intRow(PRODUCT_ID) + "}) " +
//						"MERGE (p)-[:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]->(i)"
//		)));

		session.run(new Query(withLoadStatement(
				"MERGE (p:" + PZN_LABEL + " {code: " + row(PZN) + "}) " +
						"ON CREATE SET p:" + CODING_SYSTEM_LABEL
		)));

		System.out.println(withLoadStatement(
				"MATCH (p:" + PZN_LABEL + " {code: " + row(PZN) + "}) " +
						"MATCH (i:" + PRODUCT_LABEL + " {mmiId: " + intRow(PRODUCT_ID) + "}) " +
						"MERGE (p)-[:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]->(i)"
		));

		session.run(new Query(withLoadStatement(
				"MATCH (p:" + PZN_LABEL + " {code: " + row(PZN) + "}) " +
						"MATCH (i:" + PRODUCT_LABEL + " {mmiId: " + intRow(PRODUCT_ID) + "}) " +
						"MERGE (p)-[:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]->(i)"
		)));
	}
}
