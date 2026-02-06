package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions;
import org.neo4j.driver.Session;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.ARCHIVED_ATTR;

/**
 * This loader uses the ARCHIVE_PACKAGE table to create package and PZN nodes and make them point to the corresponding
 * product nodes. This must run after archived products have been loaded.
 *
 * @author Markus Budeus
 */
public class ArchivePackageLoader extends CsvLoader {

	private static final String MMI_ID = "ID";
	private static final String PRODUCT_ID = "PRODUCTID";
	private static final String PZN = "PZN";
	private static final String NAME = "NAME";
	private static final String ON_MARKET_DATE = "ONMARKETDATE";
	private static final String OFF_MARKET_DATE = "OFFMARKETDATE";
	private static final String PZN_SUCCESSOR = "PZNSUCCESSOR";

	public ArchivePackageLoader(Session session) {
		super("ARCHIVE_PACKAGE.CSV", session);
	}

	@Override
	protected void executeLoad() {
		// We noticed that different packages always have a different PZN, so we assume this is the case
		// Also, a non-archived package with the same MMI id wins

		startSubtask("Loading CSV file");
		executeQuery(withLoadStatement(
				"MERGE (p:"+DatabaseDefinitions.PACKAGE_LABEL+" { mmiId: "+intRow(MMI_ID)+"}) " +
						"ON CREATE SET " +
						"p:Temp, " +
						"p.productId = " + intRow(PRODUCT_ID) + ", " +
						"p.pzn = " + nullIfBlank(row(PZN)) + ", " +
						"p.name = " + nullIfBlank(row(NAME)) + ", " +
						"p.onMarketDate = " + nullIfBlank(row(ON_MARKET_DATE)) + ", " +
						"p.offMarketDate = " + row(OFF_MARKET_DATE) + ", " +
						"p.pznSuccessor = " + nullIfBlank(row(PZN_SUCCESSOR)) + ", " +
						"p."+ARCHIVED_ATTR + " = true"
		));

		startSubtask(
				"Connecting " + DatabaseDefinitions.PACKAGE_LABEL + " to " + DatabaseDefinitions.PRODUCT_LABEL + " nodes");
		executeQuery(
				"MATCH (p:" + DatabaseDefinitions.PACKAGE_LABEL + ":Temp) " +
						"MATCH (i:" + DatabaseDefinitions.PRODUCT_LABEL + " {mmiId: p.productId}) " +
						withRowLimit(
								"WITH p, i CREATE (p)-[:" + DatabaseDefinitions.PACKAGE_BELONGS_TO_PRODUCT_LABEL + "]->(i) "));

		startSubtask("Deleting unmatched " + DatabaseDefinitions.PACKAGE_LABEL + " nodes");
		executeQuery(
				"MATCH (p:" + DatabaseDefinitions.PACKAGE_LABEL + ":Temp) " +
						"WHERE NOT (p)-[:" + DatabaseDefinitions.PACKAGE_BELONGS_TO_PRODUCT_LABEL + "]->(:" + DatabaseDefinitions.PRODUCT_LABEL + ") " +
						withRowLimit("WITH p DELETE p")
		);

		startSubtask("Parsing onMarketDate");
		executeQuery(
				"MATCH (p:" + DatabaseDefinitions.PACKAGE_LABEL + ":Temp) WHERE NOT p.onMarketDate IS NULL " +
						withRowLimit("WITH p WITH p, split(p.onMarketDate, '.') AS dateParts " +
								"SET p.onMarketDate = date(dateParts[2]+'-'+dateParts[1]+'-'+dateParts[0])")
		);
		startSubtask("Parsing offMarketDate");
		executeQuery(
				"MATCH (p:" + DatabaseDefinitions.PACKAGE_LABEL + ":Temp) " +
						withRowLimit("WITH p WITH p, split(p.offMarketDate, '.') AS dateParts " +
								"SET p.offMarketDate = date(dateParts[2]+'-'+dateParts[1]+'-'+dateParts[0])")
		);

		startSubtask("Creating PZN nodes");
		executeQuery(
				"MATCH (p:" + DatabaseDefinitions.PACKAGE_LABEL + ":Temp) WHERE NOT p.pzn IS NULL " +
						withRowLimit(
								"WITH p CREATE (c:" + DatabaseDefinitions.PZN_LABEL + ":" + DatabaseDefinitions.CODE_LABEL + " {" +
										"code: p.pzn " +
										"}) " +
										"CREATE (c)-[:" + DatabaseDefinitions.CODE_REFERENCE_RELATIONSHIP_NAME + "]->(p)")
		);

		startSubtask("Interconnecting Package nodes");
		executeQuery(
				"MATCH (p1:" + DatabaseDefinitions.PZN_LABEL + ")-[:" + DatabaseDefinitions.CODE_REFERENCE_RELATIONSHIP_NAME + "]->(pk1:" + DatabaseDefinitions.PACKAGE_LABEL + ":Temp) " +
						"MATCH (p2:" + DatabaseDefinitions.PZN_LABEL + " {code: pk1.pznSuccessor})-[:" + DatabaseDefinitions.CODE_REFERENCE_RELATIONSHIP_NAME + "]->(pk2:" + DatabaseDefinitions.PACKAGE_LABEL + ") " +
						withRowLimit(
								"WITH pk1, pk2 CREATE (pk1)-[:" + DatabaseDefinitions.PACKAGE_HAS_SUCCESSOR_LABEL + "]->(pk2)")
		);

		startSubtask("Cleaning up");
		// Remove obsolete fields
		executeQuery(
				"MATCH (p:" + DatabaseDefinitions.PACKAGE_LABEL + ":Temp) " +
						withRowLimit("WITH p REMOVE p:Temp, p.productId, p.pzn, p.pznSuccessor"));
	}
}
