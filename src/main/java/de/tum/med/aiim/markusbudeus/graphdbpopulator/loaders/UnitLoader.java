package de.tum.med.aiim.markusbudeus.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.io.IOException;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.UNIT_LABEL;

/**
 * Loads all existing units from the MMI PharmIndex catalog.
 */
public class UnitLoader extends CatalogEntryLoader {

	/**
	 * The catalog id of the molecule units.
	 */
	private static final int UNIT_CATALOG_ID = 107;

	public UnitLoader(Session session) throws IOException {
		super(session);
	}

	@Override
	protected void executeLoad() {
		executeQuery(
				"CREATE CONSTRAINT unitMmiCodeConstraint IF NOT EXISTS FOR (u:" + UNIT_LABEL + ") REQUIRE u.mmiCode IS UNIQUE"
		);
		executeQuery(withFilteredLoadStatement(UNIT_CATALOG_ID,
						"CREATE (u:" + UNIT_LABEL +
						" {mmiCode: " + row(CODE) + ", mmiName: " + row(NAME) + ", print: " + row(NAME) + "})"
		));
	}

}
