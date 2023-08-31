package de.tum.markusbudeus.migrators;

import org.neo4j.driver.Query;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.io.IOException;
import java.nio.file.Path;

import static de.tum.markusbudeus.DatabaseDefinitions.*;
import static org.neo4j.driver.Values.parameters;

/**
 * This migrator creates Drug nodes using the ITEM table in the MMI database and connects them to the Product nodes.
 * Requires product nodes to already exist.
 */
public class DrugMigrator extends Migrator {

	private static final int ID_INDEX = 0;
	private static final int PRODUCT_ID_INDEX = 2;

	public DrugMigrator(Path directory, Session session)
	throws IOException {
		super(directory, "ITEM.CSV", session);
	}

	@Override
	public void migrateLine(String[] line) {
		addNode(
				Integer.parseInt(line[ID_INDEX]),
				Integer.parseInt(line[PRODUCT_ID_INDEX])
		);
	}

	private void addNode(int id, int productId) {
		Result result = session.run(new Query(
				"CREATE (d:" + DRUG_LABEL + " {mmi_id: $mmi_id}) " +
						"WITH d " +
						"MATCH (p:" + PRODUCT_LABEL + " {mmi_id: $product_id}) " +
						"CREATE (p)-[r:" + PRODUCT_CONTAINS_DRUG_LABEL + "]->(d) " +
						"RETURN p, d, r",
				parameters("mmi_id", id, "product_id", productId)));

		// Verify only one relation has been created

		if (!result.hasNext()) {
			System.err.println("Warning: No product to which the item " + id + " belongs has been found.");
		}
		result.next();
		if (result.hasNext()) {
			System.err.println("Warning: The item " + id + " has been matched to more than one product!");
		}
		result.consume();
	}

}
