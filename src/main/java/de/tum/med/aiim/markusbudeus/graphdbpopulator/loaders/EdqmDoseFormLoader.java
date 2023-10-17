package de.tum.med.aiim.markusbudeus.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.io.IOException;
import java.nio.file.Path;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.*;

/**
 * Creates EDQM Dose form nodes and links them to the corresponding MMI dose form nodes using the dose_form_mapping.csv
 * file. Requires the MMI Dose form nodes to already exist.
 */
public class EdqmDoseFormLoader extends Loader {

	private static final String MMI_CODE = "MMICODE";
	private static final String EDQM_CODE = "EDQMCODE";
	private static final String EDQM_NAME = "NAME";
	private static final String EDQM_STATUS = "STATUS";
	private static final String EDQM_INTENTED_SITE = "INTENDEDSITE";

	public EdqmDoseFormLoader(Session session) throws IOException {
		super(Path.of("dose_form_mapping.csv"), session);
	}

	@Override
	protected void executeLoad() {
		executeQuery(
				"CREATE CONSTRAINT edqmCodeConstraint IF NOT EXISTS FOR (e:" + EDQM_LABEL + ") REQUIRE e.code IS UNIQUE"
		);
		executeQuery(
				"CREATE CONSTRAINT edqmNameConstraint IF NOT EXISTS FOR (e:" + EDQM_LABEL + ") REQUIRE e.name IS UNIQUE"
		);
		executeQuery(withLoadStatement(
				"WITH " + ROW_IDENTIFIER + " WHERE " + row(EDQM_CODE) + " IS NOT NULL " +
						"MERGE (e:" + EDQM_LABEL + " {code: " + row(EDQM_CODE) + "}) " +
						"ON CREATE SET " +
						"e.name = " + row(EDQM_NAME) + ", " +
						"e.status = " + row(EDQM_STATUS) + ", " +
						"e.intendedSite = " + row(EDQM_INTENTED_SITE) + ", " +
						"e:" + CODE_LABEL + " " +
						"WITH " + ROW_IDENTIFIER + ", e " +
						"MATCH (d:" + DOSE_FORM_LABEL + " {mmiCode: " + row(MMI_CODE) + "}) " +
						"CREATE (d)-[:" + DOSE_FORM_IS_EDQM + "]->(e)",
				','
		));

		executeQuery(
				"CREATE (cs:" + CODING_SYSTEM_LABEL + " {" +
						"uri: 'standardterms.edqm.eu', " +
						"name: 'EDQM Standard Terms database', " +
						"date: date('2023-10-16'), " +
						"notice: 'Data is taken from the EDQM Standard Terms database and is reproduced with permission " +
						"of the European Directorate for the Quality of Medicines & HealthCare, Council of Europe (EDQM). " +
						"The data has been retrieved at the date given by the date property. Since the EDQM Standard " +
						"Terms database is not a static list, this data may not be up to date.'" +
						"}) " +
						"WITH cs " +
						"MATCH (e:" + EDQM_LABEL + ") " +
						"CREATE (e)-[:" + BELONGS_TO_CODING_SYSTEM_LABEL + "]->(cs)"
		);
	}
}
