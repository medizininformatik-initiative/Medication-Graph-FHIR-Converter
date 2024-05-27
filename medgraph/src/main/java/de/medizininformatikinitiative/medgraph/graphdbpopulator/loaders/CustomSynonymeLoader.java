package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.io.IOException;
import java.nio.file.Path;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;

/**
 * Loads custom synonymes given in the custom_synonymes.csv file. Requires the target nodes of these synonymes to
 * already exist.
 *
 * @author Markus Budeus
 */
public class CustomSynonymeLoader extends CsvLoader {

	private static final String SUBSTANCE_TYPE = "S";
	private static final String PRODUCT_TYPE = "P";
	private static final String TYPE = "Type";
	private static final String MMI_ID = "MmiId";
	private static final String SYNONYME = "Synonyme";

	public CustomSynonymeLoader(Session session) {
		super(Path.of("custom_synonymes.csv"), session);
	}

	@Override
	protected void executeLoad() {
		executeForType(SUBSTANCE_LABEL, SUBSTANCE_TYPE);
		executeForType(PRODUCT_LABEL, PRODUCT_TYPE);
	}

	private void executeForType(String nodeLabel, String typeFilter) {
		executeQuery(withLoadStatement(
				" WITH " + ROW_IDENTIFIER + " WHERE " + row(TYPE) + " = '" + typeFilter + "' " +
						"MATCH (t:" + nodeLabel + " {mmiId: " + intRow(MMI_ID) + "}) " +
						"MERGE (sy:" + SYNONYME_LABEL + " {name: " + row(SYNONYME) + "}) " +
						"MERGE (sy)-[:"+SYNONYME_REFERENCES_NODE_LABEL+"]->(t)"
		));
	}

}
