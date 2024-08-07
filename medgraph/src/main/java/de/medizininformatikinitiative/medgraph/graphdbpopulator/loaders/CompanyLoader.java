package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.COMPANY_LABEL;

/**
 * Creates company/manufacturer nodes using the company csv.
 *
 * @author Markus Budeus
 */
public class CompanyLoader extends CsvLoader {

	private static final String ID = "ID";
	private static final String NAME = "NAME";
	private static final String SHORT_NAME = "SHORTNAME";

	public CompanyLoader(Session session) {
		super("COMPANY.CSV", session);
	}

	@Override
	protected void executeLoad() {
		executeQuery(
				"CREATE CONSTRAINT companyMmiIdConstraint IF NOT EXISTS FOR (c:" + COMPANY_LABEL + ") REQUIRE c.mmiId IS UNIQUE"
		);
		executeQuery(withLoadStatement(
				"CREATE (c:" + COMPANY_LABEL +
						" {name: " + nullIfBlank(row(NAME)) + ", shortName: " + nullIfBlank(row(SHORT_NAME)) + ", mmiId: " + intRow(ID) + "})"
		));
	}
}
