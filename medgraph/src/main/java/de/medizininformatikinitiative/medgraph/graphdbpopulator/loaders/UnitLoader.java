package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions;
import org.neo4j.driver.Session;

import java.io.IOException;

/**
 * Loads all existing units from the MMI PharmIndex catalog.
 *
 * @author Markus Budeus
 */
public class UnitLoader extends CatalogEntryLoader {

	/**
	 * The catalog id of the molecule units.
	 */
	private static final int UNIT_CATALOG_ID = 107;

	public UnitLoader(Session session) {
		super(session);
	}

	@Override
	protected void executeLoad() {
		executeQuery(
				"CREATE CONSTRAINT unitMmiCodeConstraint IF NOT EXISTS FOR (u:" + DatabaseDefinitions.UNIT_LABEL + ") REQUIRE u.mmiCode IS UNIQUE"
		);
		executeQuery(withFilteredLoadStatement(UNIT_CATALOG_ID,
				"CREATE (u:" + DatabaseDefinitions.UNIT_LABEL +
						" {mmiCode: " + row(CODE) +
						", mmiName: " + row(NAME) +
						", name: " + row(NAME) +
						", print: " + row(NAME) + "})"
		));
	}

}
