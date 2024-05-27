package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.io.IOException;
import java.nio.file.Path;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;

/**
 * Creates EDQM Dose form nodes and links them to the corresponding MMI dose form nodes using the dose_form_mapping.csv
 * file. Requires the MMI Dose form nodes to already exist.
 *
 * @author Markus Budeus
 */
public class EdqmDoseFormLoader extends CsvLoader {

	// TODO Remake with new EDQM data

	private static final String MMI_CODE = "MMICODE";
	private static final String EDQM_CODE = "EDQMCODE";
	private static final String EDQM_NAME = "NAME";
	private static final String EDQM_STATUS = "STATUS";
	private static final String EDQM_INTENTED_SITE = "INTENDEDSITE";

	public EdqmDoseFormLoader(Session session) {
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

		// Create EQDM Nodes and connect them to MMI Dose Form Nodes
		executeQuery(withLoadStatement(
				"WITH " + ROW_IDENTIFIER + " WHERE " + nullIfBlank(row(EDQM_CODE)) + " IS NOT NULL " +
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
	}
}
