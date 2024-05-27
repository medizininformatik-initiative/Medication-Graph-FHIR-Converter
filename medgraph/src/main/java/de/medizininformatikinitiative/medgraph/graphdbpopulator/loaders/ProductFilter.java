package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.io.IOException;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.PRODUCT_LABEL;

/**
 * This class uses the PRODUCT_FLAG.CSV to remove all products which are not pharmaceutical products.
 *
 * @author Markus Budeus
 */
public class ProductFilter extends CsvLoader {

	private static final String PRODUCT_ID = "PRODUCTID";
	private static final String PHARMACEUTICAL_FLAG = "PHARMACEUTICAL_FLAG";

	public ProductFilter(Session session) {
		super("PRODUCT_FLAG.CSV", session);
	}

	@Override
	protected void executeLoad() {
		executeQuery(withLoadStatement(
				"WITH " + ROW_IDENTIFIER + " WHERE " + row(PHARMACEUTICAL_FLAG) + "= '0'" +
						"MATCH (p:" + PRODUCT_LABEL + " {mmiId: " + intRow(PRODUCT_ID) + "}) " +
						"DELETE p"
		));
	}
}
