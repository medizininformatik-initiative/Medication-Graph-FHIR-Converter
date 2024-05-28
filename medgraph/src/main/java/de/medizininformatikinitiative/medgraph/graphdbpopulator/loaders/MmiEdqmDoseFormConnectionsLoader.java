package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.nio.file.Path;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;

/**
 * Links EDQM Standard Terms PDF nodes to the corresponding MMI dose form nodes using the dose_form_mapping.csv
 * file. Requires the MMI Dose form nodes as well as the EDQM Standard Term nodes to already exist.
 *
 * @author Markus Budeus
 */
public class MmiEdqmDoseFormConnectionsLoader extends CsvLoader {

	private static final String MMI_CODE = "MMICODE";
	private static final String EDQM_CODE = "EDQMCODE";

	public MmiEdqmDoseFormConnectionsLoader(Session session) {
		super(Path.of("dose_form_mapping.csv"), session);
	}

	@Override
	protected void executeLoad() {
		executeQuery(withLoadStatement(
				"WITH " + ROW_IDENTIFIER + " WHERE " + nullIfBlank(row(EDQM_CODE)) + " IS NOT NULL " +
						"MATCH (e:" + EDQM_LABEL + " {code: " + row(EDQM_CODE) + "}) " +
						"MATCH (d:" + MMI_DOSE_FORM_LABEL + " {mmiCode: " + row(MMI_CODE) + "}) " +
						"CREATE (d)-[:" + DOSE_FORM_IS_EDQM + "]->(e)",
				','
		));
	}
}
