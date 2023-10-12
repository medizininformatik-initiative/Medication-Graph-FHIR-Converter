package de.tum.med.aiim.markusbudeus.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.io.IOException;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.COMPANY_LABEL;

/**
 * Creates company/manufacturer nodes using the company csv.
 */
public class CompanyLoader extends Loader {

	private static final String ID = "ID";
	private static final String NAME = "NAME";
	private static final String SHORT_NAME = "SHORTNAME";

	public CompanyLoader(Session session) throws IOException {
		super("COMPANY.CSV", session);
	}

	@Override
	protected void executeLoad() {
		executeQuery(
				"CREATE CONSTRAINT companyMmiIdConstraint IF NOT EXISTS FOR (c:" + COMPANY_LABEL + ") REQUIRE c.mmiId IS UNIQUE"
		);
		executeQuery(withLoadStatement(
				"CREATE (c:" + COMPANY_LABEL +
						" {name: " + row(NAME) + ", shortName: " + row(SHORT_NAME) + ", mmiId: " + intRow(ID) + "})"
		));
	}
}
