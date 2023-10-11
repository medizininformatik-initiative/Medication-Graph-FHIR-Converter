package de.tum.med.aiim.markusbudeus.graphdbpopulator.loaders;

import de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions;
import org.neo4j.driver.Query;
import org.neo4j.driver.Session;

import java.io.IOException;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.PRODUCT_LABEL;

/**
 * This class creates the Product nodes in the database using the PRODUCT table from the MMI PharmIndex.
 */
public class ProductLoader extends Loader {

	private static final String ID = "ID";
	private static final String NAME = "NAME_PLAIN";

	public ProductLoader(Session session) throws IOException {
		super("PRODUCT.CSV", session);
	}

	@Override
	protected void executeLoad() {
		session.run(new Query(
				"CREATE CONSTRAINT productMmiIdConstraint IF NOT EXISTS FOR (p:" + PRODUCT_LABEL + ") REQUIRE p.mmiId IS UNIQUE"
		));
		session.run(new Query(withLoadStatement(
				"CREATE (d:" + DatabaseDefinitions.PRODUCT_LABEL + " {name: " + row(NAME) + ", mmiId: " + intRow(
						ID) + "})"
		)));
	}
}
