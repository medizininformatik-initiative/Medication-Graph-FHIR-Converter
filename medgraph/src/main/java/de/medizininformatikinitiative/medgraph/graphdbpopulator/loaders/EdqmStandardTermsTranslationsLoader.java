package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.nio.file.Path;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;

/**
 * Loads the translations from the edqm_translations.csv file and applies them as synonymes to the corresponding EDQM
 * nodes. Requires the EDQM nodes to already exist.
 *
 * @author Markus Budeus
 */
public class EdqmStandardTermsTranslationsLoader extends CsvLoader {
	private static final String TARGET_CLASS = "CLASS";
	private static final String TARGET_CODE = "CODE";
	private static final String SYNONYME = "SYNONYME";

	public EdqmStandardTermsTranslationsLoader(Session session) {
		super(Path.of("edqm_translations.csv"), session);
	}

	@Override
	protected void executeLoad() {
		executeQuery(withLoadStatement(
				"MATCH (e:" + EDQM_LABEL + " {code: "+ row(TARGET_CLASS) + " + '-' + " + row(TARGET_CODE)+ "}) " +
						"MERGE (s:"+SYNONYME_LABEL+"{name: "+row(SYNONYME) + "}) " +
						"CREATE (s)-[:"+SYNONYME_REFERENCES_NODE_LABEL+"]->(e)"
		));

	}
}
