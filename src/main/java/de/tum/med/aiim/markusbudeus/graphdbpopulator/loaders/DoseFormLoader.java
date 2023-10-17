package de.tum.med.aiim.markusbudeus.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.io.IOException;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.DOSE_FORM_LABEL;

/**
 * This loader creates dose form nodes for all MMI PharmIndex dose forms from the catalog.
 */
public class DoseFormLoader extends CatalogEntryLoader {

		/**
		 * The catalog id of the dose forms.
		 */
		private static final String DOSE_FORM_CATALOG_ID = "104";

		private static final String CATALOG_ID = "CATALOGID";
		private static final String CODE = "CODE";
		private static final String NAME = "NAME";
		private static final String DESC = "DESCRIPTION";

		public DoseFormLoader(Session session) throws IOException {
			super(session);
		}

		@Override
		protected void executeLoad() {
			executeQuery(
					"CREATE CONSTRAINT doseFormMmiCodeConstraint IF NOT EXISTS FOR (u:" + DOSE_FORM_LABEL + ") REQUIRE u.mmiCode IS UNIQUE"
			);
			executeQuery(withLoadStatement(
					"WITH " + ROW_IDENTIFIER + " WHERE " + row(CATALOG_ID) + " = '" + DOSE_FORM_CATALOG_ID + "' " +
							"CREATE (d:" + DOSE_FORM_LABEL +
							" {mmiCode: " + row(CODE) + ", mmiName: " + row(NAME) + ", mmiDesc: "+nullIfBlank(row(DESC))+"})"
			));
		}
}
