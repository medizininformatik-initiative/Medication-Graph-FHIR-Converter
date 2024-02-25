package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.io.IOException;

import static de.medizininformatikinitiative.medgraph.graphdbpopulator.DatabaseDefinitions.*;
import static de.medizininformatikinitiative.medgraph.graphdbpopulator.DatabaseDefinitions.PRODUCT_CONTAINS_DRUG_LABEL;

/**
 * This loader creates Drug nodes using the ITEM table in the MMI database and connects them to the Product nodes.
 * Requires product nodes to already exist.
 * <p>
 * Also connects them to the corresponding dose form and amount unit nodes. The MMI Dose form nodes as well as the unit
 * nodes must already exist for that.
 *
 * @author Markus Budeus
 */
public class DrugLoader extends CsvLoader {

	private static final String ID = "ID";
	private static final String PRODUCT_ID = "PRODUCTID";

	private static final String AMOUNT = "BASECOUNT";
	private static final String AMOUNT_UNIT_CODE = "BASEMOLECULEUNITCODE";
	private static final String DOSE_FORM_CODE = "PHARMFORMCODE";

	public DrugLoader(Session session) throws IOException {
		super("ITEM.CSV", session);
	}

	@Override
	protected void executeLoad() {
		executeQuery(
				"CREATE CONSTRAINT drugMmiIdConstraint IF NOT EXISTS FOR (d:" + DRUG_LABEL + ") REQUIRE d.mmiId IS UNIQUE"
		);

		// Create Nodes
		startSubtask("Creating nodes");
		executeQuery(withLoadStatement(
				"CREATE (d:" + DRUG_LABEL + " {mmiId: " + intRow(ID) + ", " +
						"amount: " + nullIfBlank(row(AMOUNT)) + ", " +
						"mmiUnitCode: " + nullIfBlank(row(AMOUNT_UNIT_CODE)) + "," +
						"mmiDoseFormCode: " + nullIfBlank(row(DOSE_FORM_CODE)) + "})"
		));

		startSubtask("Connecting to product nodes");
		// Connect to Product Nodes
		executeQuery(withLoadStatement(
				"MATCH (d:" + DRUG_LABEL + " {mmiId: " + intRow(ID) + "}) " +
						"MATCH (p:" + PRODUCT_LABEL + " {mmiId: " + intRow(PRODUCT_ID) + "}) " +
						"WITH d, p " +
						"CREATE (p)-[r:" + PRODUCT_CONTAINS_DRUG_LABEL + "]->(d)"
		));

		startSubtask("Removing orphan drug nodes");
		executeQuery(
				"MATCH (d:"+DRUG_LABEL+") " +
						"WHERE NOT (:"+PRODUCT_LABEL+")-[:"+PRODUCT_CONTAINS_DRUG_LABEL+"]->(d) " +
						"DELETE d"
		);

		startSubtask("Connecting to unit nodes");
		// Connect to Unit Nodes
		executeQuery(
				"MATCH (d:"+DRUG_LABEL+") WHERE d.mmiUnitCode IS NOT NULL " +
						"MATCH (u:"+UNIT_LABEL+" {mmiCode: d.mmiUnitCode}) " +
						"WITH d, u " +
						"CREATE (d)-[:"+DRUG_HAS_UNIT_LABEL+"]->(u) " +
						"SET d.mmiUnitCode = null"
		);

		startSubtask("Connecting to dose form nodes");
		// Connect to Dose Form nodes
		executeQuery(
				"MATCH (d:"+DRUG_LABEL+") WHERE d.mmiDoseFormCode IS NOT NULL " +
						"MATCH (f:"+DOSE_FORM_LABEL+" {mmiCode: d.mmiDoseFormCode}) " +
						"WITH d, f " +
						"CREATE (d)-[:"+DRUG_HAS_DOSE_FORM_LABEL+"]->(f) " +
						"SET d.mmiDoseFormCode = null"
		);
	}
}
