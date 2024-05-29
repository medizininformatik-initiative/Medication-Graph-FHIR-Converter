package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.nio.file.Path;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;
import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.EDQM_LABEL;

/**
 * Loads the hand-crafted synonymes for EDQM Standard Term concepts from the dose_form_synonymes.csv file.
 * Requires the EDQM Standard Term nodes to already exist.
 *
 * @author Markus Budeus
 */
public class EdqmStandardTermsCustomSynonymesLoader extends CsvLoader {

	private static final String SYNONYME = "SYNONYME";
	private static final String TARGET_CODE = "TARGETCODE";

	public EdqmStandardTermsCustomSynonymesLoader(Session session) {
		super(Path.of("dose_form_synonymes.csv"), session);
	}

	@Override
	protected void executeLoad() {
		executeQuery(withLoadStatement(
				"MATCH (e:" + EDQM_LABEL + " {code: " + row(TARGET_CODE)+ "}) " +
						"MERGE (s:"+SYNONYME_LABEL+"{name: "+row(SYNONYME) + "}) " +
						"MERGE (s)-[:"+SYNONYME_REFERENCES_NODE_LABEL+"]->(e)"
		));

	}

}
