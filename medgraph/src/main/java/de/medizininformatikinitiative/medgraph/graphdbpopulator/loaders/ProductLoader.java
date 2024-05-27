package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions;
import org.neo4j.driver.Session;

import java.io.IOException;

/**
 * This class creates the Product nodes in the database using the PRODUCT table from the MMI Pharmindex.
 *
 * @author Markus Budeus
 */
public class ProductLoader extends CsvLoader {

	private static final String ID = "ID";
	private static final String NAME = "NAME_PLAIN";

	public ProductLoader(Session session) {
		super("PRODUCT.CSV", session);
	}

	@Override
	protected void executeLoad() {
		executeQuery(
				"CREATE CONSTRAINT productMmiIdConstraint IF NOT EXISTS FOR (p:" + DatabaseDefinitions.PRODUCT_LABEL + ") REQUIRE p.mmiId IS UNIQUE"
		);
		executeQuery(withLoadStatement(
				"CREATE (d:" + DatabaseDefinitions.PRODUCT_LABEL + " {name: " + row(NAME) + ", mmiId: " + intRow(
						ID) + "})"
		));
	}
}
