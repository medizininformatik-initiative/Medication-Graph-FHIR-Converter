package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.io.IOException;

import static de.medizininformatikinitiative.medgraph.graphdbpopulator.DatabaseDefinitions.DOSE_FORM_LABEL;

/**
 * This loader creates dose form nodes for all MMI PharmIndex dose forms from the catalog.
 *
 * @author Markus Budeus
 */
public class DoseFormLoader extends CatalogEntryLoader {

	/**
	 * The catalog id of the dose forms.
	 */
	private static final int DOSE_FORM_CATALOG_ID = 104;

	public DoseFormLoader(Session session) throws IOException {
		super(session);
	}

	@Override
	protected void executeLoad() {
		executeQuery(
				"CREATE CONSTRAINT doseFormMmiCodeConstraint IF NOT EXISTS FOR (u:" + DOSE_FORM_LABEL + ") REQUIRE u.mmiCode IS UNIQUE"
		);
		executeQuery(withFilteredLoadStatement(DOSE_FORM_CATALOG_ID,
				"CREATE (d:" + DOSE_FORM_LABEL +
						" {mmiCode: " + row(CODE) + ", mmiName: " + row(NAME) + ", mmiDesc: " + nullIfBlank(
						row(DESC)) + "})"
		));
	}
}
