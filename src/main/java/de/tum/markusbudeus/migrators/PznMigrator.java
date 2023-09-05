package de.tum.markusbudeus.migrators;

import org.neo4j.driver.Query;
import org.neo4j.driver.Session;

import java.io.IOException;
import java.nio.file.Path;

import static de.tum.markusbudeus.DatabaseDefinitions.*;
import static org.neo4j.driver.Values.parameters;

/**
 * This migrator uses the PACKAGE table to create PZN nodes and make them point to the corresponding product nodes.
 */
public class PznMigrator extends Migrator {

	private static final int PRODUCT_ID_INDEX = 1;
	private static final int PZN_INDEX = 2;

	public PznMigrator(Path directory, Session session) throws IOException {
		super(directory, "PACKAGE.CSV", session);
	}

	@Override
	public void migrateLine(String[] line) {
		addNode(
				line[PZN_INDEX],
				Long.parseLong(line[PRODUCT_ID_INDEX])
		);
	}

	private void addNode(String pzn, long productId) {
		session.run(new Query(
				"MERGE (p:" + PZN_LABEL + ":" + CODING_SYSTEM_LABEL + " {code: $code} " +
						"WITH p " +
						"MATCH (i:" + PRODUCT_LABEL + " {mmi_id: $product_id})" +
						"MERGE (p)-[r:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]->(i)",
				parameters("code", pzn, "product_id", productId)
		));
	}

}
