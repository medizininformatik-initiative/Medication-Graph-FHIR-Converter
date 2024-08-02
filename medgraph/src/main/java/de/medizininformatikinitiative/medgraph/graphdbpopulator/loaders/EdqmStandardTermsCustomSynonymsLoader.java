package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.nio.file.Path;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;
import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.EDQM_LABEL;

/**
 * Loads the hand-crafted synonyms for EDQM Standard Term concepts from the dose_form_synonyms.csv file.
 * Requires the EDQM Standard Term nodes to already exist.
 *
 * @author Markus Budeus
 */
public class EdqmStandardTermsCustomSynonymsLoader extends CsvLoader {

	private static final String SYNONYM = "SYNONYM";
	private static final String TARGET_CODE = "TARGETCODE";

	public EdqmStandardTermsCustomSynonymsLoader(Session session) {
		super(Path.of("dose_form_synonyms.csv"), session);
	}

	@Override
	protected void executeLoad() {
		executeQuery(withLoadStatement(
				"MATCH (e:" + EDQM_LABEL + " {code: " + row(TARGET_CODE)+ "}) " +
						"MERGE (s:"+ SYNONYM_LABEL +"{name: "+row(SYNONYM) + "}) " +
						"MERGE (s)-[:"+ SYNONYM_REFERENCES_NODE_LABEL +"]->(e)"
		));

	}

}
