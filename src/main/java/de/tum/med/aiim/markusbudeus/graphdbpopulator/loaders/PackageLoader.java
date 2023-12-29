package de.tum.med.aiim.markusbudeus.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.io.IOException;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.*;

/**
 * This loader uses the PACKAGE table to create PZN nodes and make them point to the corresponding product nodes.
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

	public PackageLoader(Session session) throws IOException {
		super("PACKAGE.CSV", session);
	}

	@Override
	protected void executeLoad() {
		// We noticed that different packages always have a different PZN, so we assume this is the case
		executeQuery(
				"CREATE CONSTRAINT pznCodeConstraint IF NOT EXISTS FOR (p:" + PZN_LABEL + ") REQUIRE p.code IS UNIQUE"
		);
		executeQuery(
				"CREATE CONSTRAINT pznConstraint IF NOT EXISTS FOR (p:" + PZN_LABEL + ") REQUIRE p.pzn IS UNIQUE"
		);
		executeQuery(
				"CREATE CONSTRAINT packageMmiIdConstraint IF NOT EXISTS FOR (p:" + PACKAGE_LABEL + ") REQUIRE p.mmiId IS UNIQUE"
		);

		startSubtask("Loading CSV file");
		executeQuery(withLoadStatement(
				"CREATE (p:" + PACKAGE_LABEL + " {" +
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

		startSubtask("Connecting "+PACKAGE_LABEL+" to "+PRODUCT_LABEL+" nodes");
		executeQuery(
				"MATCH (p:" + PACKAGE_LABEL +") " +
						"MATCH (i:" + PRODUCT_LABEL + " {mmiId: p.productId}) " +
						"CREATE (p)-[:" + PACKAGE_BELONGS_TO_PRODUCT_LABEL + "]->(i) "
		);

		startSubtask("Deleting unmatched "+PACKAGE_LABEL+" nodes");
		executeQuery(
				"MATCH (p:"+PACKAGE_LABEL+") " +
						"WHERE NOT (p)-[:"+PACKAGE_BELONGS_TO_PRODUCT_LABEL+"]->(:"+PRODUCT_LABEL+") " +
						"DELETE p"
		);

		startSubtask("Parsing onMarketDate");
		// Parse onMarketDate
		executeQuery(
				"MATCH (p:"+PACKAGE_LABEL+") WHERE NOT p.onMarketDate IS NULL " +
						"WITH p, split(p.onMarketDate, '.') AS dateParts " +
						"SET p.onMarketDate = date(dateParts[2]+'-'+dateParts[1]+'-'+dateParts[0])"
		);

		startSubtask("Cleaning up");
		// Assign PZN and Code labels if required
		executeQuery(
				"MATCH (p:" + PACKAGE_LABEL + ") WHERE NOT p.pzn IS NULL " +
						"SET p:" + PZN_LABEL + ", p:" + CODE_LABEL + ", p.code = p.pzn"
		);

		// Connect successors
		executeQuery("MATCH (p1:"+PZN_LABEL+") " +
				"MATCH (p2:"+PZN_LABEL+" {code: p1.pznSuccessor}) " +
				"CREATE (p1)-[:"+PZN_HAS_SUCCESSOR_LABEL+"]->(p2)");

		// Connect originals
		executeQuery("MATCH (p1:"+PZN_LABEL+") " +
				"MATCH (p2:"+PZN_LABEL+" {code: p1.pznOriginal}) " +
				"CREATE (p1)-[:"+PZN_IS_ORIGINALLY+"]->(p2)");

		// Remove obsolete fields
		executeQuery("MATCH (p:"+PACKAGE_LABEL+") REMOVE p.productId, p.pznSuccessor");
	}
}
