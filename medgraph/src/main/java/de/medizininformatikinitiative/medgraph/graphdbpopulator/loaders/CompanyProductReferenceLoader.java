package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.io.IOException;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;

/**
 * Uses the PRODUCT_COMPANY table from the MMI PharmIndex to create references between Product and Manufacturer nodes.
 * Requires the Product and Company nodes to already exist.
 *
 * @author Markus Budeus
 */
public class CompanyProductReferenceLoader extends CsvLoader {

	private static final String PRODUCT_ID = "PRODUCTID";
	private static final String COMPANY_ID = "COMPANYID";

	public CompanyProductReferenceLoader(Session session) {
		super("PRODUCT_COMPANY.CSV", session);
	}

	@Override
	protected void executeLoad() {
		executeQuery(withLoadStatement(
				"MATCH (c:" + COMPANY_LABEL + " {mmiId: " + intRow(COMPANY_ID) + "}) " +
						"MATCH (d:" + PRODUCT_LABEL + " {mmiId: " + intRow(PRODUCT_ID) + "}) " +
						"CREATE (c)-[r:" + MANUFACTURES_LABEL + "]->(d)"
		));
	}
}
