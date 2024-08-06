package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

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
	private static final String REFERENCE_TYPE = "PRODUCTCOMPANYTYPECODE";
	public static final String TARGET_REFERENCE_TYPE = "M"; // Manufacturers only, "S" would be distributors.

	public CompanyProductReferenceLoader(Session session) {
		super("PRODUCT_COMPANY.CSV", session);
	}

	@Override
	protected void executeLoad() {
		executeQuery(withLoadStatement(
				"WITH " + ROW_IDENTIFIER + " WHERE " + row(REFERENCE_TYPE) + " = '" + TARGET_REFERENCE_TYPE + "' " +
						"MATCH (c:" + COMPANY_LABEL + " {mmiId: " + intRow(COMPANY_ID) + "}) " +
						"MATCH (d:" + PRODUCT_LABEL + " {mmiId: " + intRow(PRODUCT_ID) + "}) " +
						"CREATE (c)-[r:" + MANUFACTURES_LABEL + "]->(d)"
		));
	}
}
