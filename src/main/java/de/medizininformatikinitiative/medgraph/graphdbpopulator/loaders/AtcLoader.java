package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import de.medizininformatikinitiative.medgraph.graphdbpopulator.DatabaseDefinitions;
import org.neo4j.driver.Session;

import java.io.IOException;

/**
 * Loads all ATC codes from the Catalog. Creates ATC nodes (the full hierarchy) and the ATC Coding System Node.
 *
 * @author Markus Budeus
 */
public class AtcLoader extends CatalogEntryLoader {

	private static final int ATC_CATALOG_ID = 17;

	public AtcLoader(Session session) throws IOException {
		super(session);
	}

	@Override
	protected void executeLoad() {
		executeQuery(
				"CREATE CONSTRAINT atcCodeConstraint IF NOT EXISTS FOR (a:" + DatabaseDefinitions.ATC_LABEL + ") REQUIRE a.code IS UNIQUE"
		);

		// Create all ATC Nodes
		executeQuery(withFilteredLoadStatement(ATC_CATALOG_ID,
				"CREATE (a:" + DatabaseDefinitions.ATC_LABEL + ":" + DatabaseDefinitions.CODE_LABEL + " {" +
						"code: " + row(CODE) + ", " +
						"description: " + row(NAME) + ", " +
						"parent: " + nullIfBlank(row(UPPER_CODE)) +
						"})"
		));

		// Create hierarchy relationships
		executeQuery("MATCH (a:" + DatabaseDefinitions.ATC_LABEL + ") WHERE a.parent IS NOT NULL " +
				"MATCH (p:" + DatabaseDefinitions.ATC_LABEL + " {code: a.parent}) " +
				withRowLimit("WITH a, p " +
						"CREATE (a)-[:" + DatabaseDefinitions.ATC_HAS_PARENT_LABEL + "]->(p)"));
	}

}
