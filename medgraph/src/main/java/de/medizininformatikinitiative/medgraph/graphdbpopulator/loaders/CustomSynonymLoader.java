package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.nio.file.Path;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;

/**
 * Loads custom synonyms given in the custom_synonyms.csv file. Requires the target nodes of these synonyms to
 * already exist.
 *
 * @author Markus Budeus
 */
public class CustomSynonymLoader extends CsvLoader {

	private static final String SUBSTANCE_TYPE = "S";
	private static final String PRODUCT_TYPE = "P";
	private static final String TYPE = "Type";
	private static final String MMI_ID = "MmiId";
	private static final String SYNONYM = "Synonym";

	public CustomSynonymLoader(Session session) {
		super(Path.of("custom_synonyms.csv"), session);
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
						"MERGE (sy:" + SYNONYM_LABEL + " {name: " + row(SYNONYM) + "}) " +
						"MERGE (sy)-[:"+ SYNONYM_REFERENCES_NODE_LABEL +"]->(t)"
		));
	}

}
