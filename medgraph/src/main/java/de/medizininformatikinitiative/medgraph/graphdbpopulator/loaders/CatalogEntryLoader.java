package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.io.IOException;

/**
 * Abstract loader which can be subclassed by loaders which want to retrieve data from a section of the catalog.
 *
 * @author Markus Budeus
 */
abstract class CatalogEntryLoader extends CsvLoader {

	protected static final String CATALOG_ID = "CATALOGID";
	protected static final String CODE = "CODE";
	protected static final String UPPER_CODE = "UPPERCODE";
	protected static final String SHORT_NAME = "SHORTNAME";
	protected static final String NAME = "NAME";
	protected static final String DESC = "DESCRIPTION";

	public CatalogEntryLoader(Session session) {
		super("CATALOGENTRY.CSV", session);
	}

	/**
	 * Just like {@link #withLoadStatement(String)}, but filters by catalog id, meaning the provided statement will only
	 * be executed for rows which match the given catalog id.
	 *
	 * @param catalogId the catalog id to filter for
	 * @param statement the statement to execute for each row
	 * @return the full Cypher statement
	 */
	protected String withFilteredLoadStatement(int catalogId, String statement) {
		return withLoadStatement(
				"WITH " + ROW_IDENTIFIER +
						" WHERE " + row(CATALOG_ID) + " = '" + catalogId + "' " + statement);
	}

}
