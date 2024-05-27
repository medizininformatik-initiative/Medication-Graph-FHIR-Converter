package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions;
import org.neo4j.driver.Session;

import java.io.IOException;

/**
 * This loader uses the PACKAGE table to create PZN nodes and make them point to the corresponding product nodes.
 *
 * @author Markus Budeus
 */
public class PackageLoader extends CsvLoader {


	private static final String MMI_ID = "ID";
	private static final String PRODUCT_ID = "PRODUCTID";
	private static final String PZN = "PZN";
	private static final String NAME = "NAME";
	private static final String ON_MARKET_DATE = "ONMARKETDATE";
	private static final String PZN_ORIGINAL = "PZNORIGINAL";
	private static final String PZN_SUCCESSOR = "PZNSUCCESSOR";
	private static final String AMOUNT_TEXT = "AMOUNTTEXT";
	private static final String AMOUNT = "AMOUNT";
	private static final String AMOUNT_F1 = "FACTOR1";
	private static final String AMOUNT_F2 = "FACTOR2";

	public PackageLoader(Session session) {
		super("PACKAGE.CSV", session);
	}

	@Override
	protected void executeLoad() {
		// We noticed that different packages always have a different PZN, so we assume this is the case
		executeQuery(
				"CREATE CONSTRAINT pznCodeConstraint IF NOT EXISTS FOR (p:" + DatabaseDefinitions.PZN_LABEL + ") REQUIRE p.code IS UNIQUE"
		);
		executeQuery(
				"CREATE CONSTRAINT pznConstraint IF NOT EXISTS FOR (p:" + DatabaseDefinitions.PZN_LABEL + ") REQUIRE p.pzn IS UNIQUE"
		);
		executeQuery(
				"CREATE CONSTRAINT packageMmiIdConstraint IF NOT EXISTS FOR (p:" + DatabaseDefinitions.PACKAGE_LABEL + ") REQUIRE p.mmiId IS UNIQUE"
		);

		startSubtask("Loading CSV file");
		executeQuery(withLoadStatement(
				"CREATE (p:" + DatabaseDefinitions.PACKAGE_LABEL + " {" +
						"mmiId: " + intRow(MMI_ID) + ", " +
						"productId: " + intRow(PRODUCT_ID) + ", " +
						"pzn: " + nullIfBlank(row(PZN)) + ", " +
						"name: " + row(NAME) + ", " +
						"onMarketDate: " + nullIfBlank(row(ON_MARKET_DATE)) + ", " +
						"pznOriginal: " + nullIfBlank(row(PZN_ORIGINAL)) + ", " +
						"pznSuccessor: " + nullIfBlank(row(PZN_SUCCESSOR)) + ", " +
						"amountText: " + row(AMOUNT_TEXT) + ", " +
						"amount: " + row(AMOUNT) + ", " +
						"amountFactor1: " + intRow(AMOUNT_F1) + ", " +
						"amountFactor2: " + intRow(AMOUNT_F2) +
						"})"
		));

		startSubtask(
				"Connecting " + DatabaseDefinitions.PACKAGE_LABEL + " to " + DatabaseDefinitions.PRODUCT_LABEL + " nodes");
		executeQuery(
				"MATCH (p:" + DatabaseDefinitions.PACKAGE_LABEL + ") " +
						"MATCH (i:" + DatabaseDefinitions.PRODUCT_LABEL + " {mmiId: p.productId}) " +
						withRowLimit(
								"WITH p, i CREATE (p)-[:" + DatabaseDefinitions.PACKAGE_BELONGS_TO_PRODUCT_LABEL + "]->(i) "));

		startSubtask("Deleting unmatched " + DatabaseDefinitions.PACKAGE_LABEL + " nodes");
		executeQuery(
				"MATCH (p:" + DatabaseDefinitions.PACKAGE_LABEL + ") " +
						"WHERE NOT (p)-[:" + DatabaseDefinitions.PACKAGE_BELONGS_TO_PRODUCT_LABEL + "]->(:" + DatabaseDefinitions.PRODUCT_LABEL + ") " +
						withRowLimit("WITH p DELETE p")
		);

		startSubtask("Parsing onMarketDate");
		// Parse onMarketDate
		executeQuery(
				"MATCH (p:" + DatabaseDefinitions.PACKAGE_LABEL + ") WHERE NOT p.onMarketDate IS NULL " +
						withRowLimit("WITH p WITH p, split(p.onMarketDate, '.') AS dateParts " +
								"SET p.onMarketDate = date(dateParts[2]+'-'+dateParts[1]+'-'+dateParts[0])")
		);

		startSubtask("Creating PZN nodes");
		executeQuery(
				"MATCH (p:" + DatabaseDefinitions.PACKAGE_LABEL + ") WHERE NOT p.pzn IS NULL " +
						withRowLimit(
								"WITH p CREATE (c:" + DatabaseDefinitions.PZN_LABEL + ":" + DatabaseDefinitions.CODE_LABEL + " {" +
										"code: p.pzn, " +
										"pznOriginal: p.pznOriginal, " +
										"pznSuccessor: p.pznSuccessor" +
										"}) " +
										"CREATE (c)-[:" + DatabaseDefinitions.CODE_REFERENCE_RELATIONSHIP_NAME + "]->(p)")
		);

		startSubtask("Interconnecting Package nodes");
		// We connect indirectly by querying the PZN nodes, because those have indexes on them and are thus queried
		// much faster.
		// Connect successors
		executeQuery(
				"MATCH (p1:" + DatabaseDefinitions.PZN_LABEL + ")-[:" + DatabaseDefinitions.CODE_REFERENCE_RELATIONSHIP_NAME + "]->(pk1:" + DatabaseDefinitions.PACKAGE_LABEL + ") " +
						"MATCH (p2:" + DatabaseDefinitions.PZN_LABEL + " {code: p1.pznSuccessor})-[:" + DatabaseDefinitions.CODE_REFERENCE_RELATIONSHIP_NAME + "]->(pk2:" + DatabaseDefinitions.PACKAGE_LABEL + ") " +
						withRowLimit(
								"WITH p1, p2 CREATE (pk1)-[:" + DatabaseDefinitions.PACKAGE_HAS_SUCCESSOR_LABEL + "]->(pk2)")
		);

		// Connect originals
		executeQuery(
				"MATCH (p1:" + DatabaseDefinitions.PZN_LABEL + ")-[:" + DatabaseDefinitions.CODE_REFERENCE_RELATIONSHIP_NAME + "]->(pk1:" + DatabaseDefinitions.PACKAGE_LABEL + ") " +
						"MATCH (p2:" + DatabaseDefinitions.PZN_LABEL + " {code: p1.pznOriginal})-[:" + DatabaseDefinitions.CODE_REFERENCE_RELATIONSHIP_NAME + "]->(pk2:" + DatabaseDefinitions.PACKAGE_LABEL + ") " +
						withRowLimit(
								"WITH p1, p2 CREATE (pk1)-[:" + DatabaseDefinitions.PACKAGE_IS_ORIGINALLY + "]->(pk2)")
		);

		startSubtask("Cleaning up");
		// Remove obsolete fields
		executeQuery(
				"MATCH (p:" + DatabaseDefinitions.PACKAGE_LABEL + ") " +
						withRowLimit("WITH p REMOVE p.productId, p.pzn, p.pznOriginal, p.pznSuccessor"));
		executeQuery(
				"MATCH (p:" + DatabaseDefinitions.PZN_LABEL + ") " +
						withRowLimit("WITH p REMOVE p.pznOriginal, p.pznSuccessor"));
	}
}
