package de.tum.med.aiim.markusbudeus.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.io.IOException;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.*;

/**
 * Loads all ATC codes from the Catalog. Creates ATC nodes (the full hierarchy) and the ATC Coding System Node.
 */
public class AtcLoader extends CatalogEntryLoader {

	private static final int ATC_CATALOG_ID = 17;

	public AtcLoader(Session session) throws IOException {
		super(session);
	}

	@Override
	protected void executeLoad() {
		executeQuery(
				"CREATE CONSTRAINT atcCodeConstraint IF NOT EXISTS FOR (a:" + ATC_LABEL + ") REQUIRE a.code IS UNIQUE"
		);

		// Create all ATC Nodes
		executeQuery(withFilteredLoadStatement(ATC_CATALOG_ID,
				"CREATE (a:" + ATC_LABEL + ":" + CODE_LABEL + " {" +
						"code: " + row(CODE) + ", " +
						"description: " + row(NAME) + ", " +
						"parent: " + nullIfBlank(row(UPPER_CODE)) +
						"})"
		));

		// Create hierarchy relationships
		executeQuery("MATCH (a:" + ATC_LABEL + ") WHERE a.parent IS NOT NULL " +
				"MATCH (p:" + ATC_LABEL + " {code: a.parent}) " +
				"WITH a, p " +
				"CREATE (a)-[:" + ATC_HAS_PARENT_LABEL + "]->(p)");
	}

}
