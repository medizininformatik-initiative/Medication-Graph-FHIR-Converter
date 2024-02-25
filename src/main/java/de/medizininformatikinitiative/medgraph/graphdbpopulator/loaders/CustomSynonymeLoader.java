package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import de.medizininformatikinitiative.medgraph.graphdbpopulator.DatabaseConnection;
import org.neo4j.driver.Session;

import java.io.IOException;
import java.nio.file.Path;

import static de.medizininformatikinitiative.medgraph.graphdbpopulator.DatabaseDefinitions.*;

/**
 * Loads custom synonymes given in the custom_synonymes.csv file. Requires the target nodes of these synonymes to
 * already exist.
 *
 * @author Markus Budeus
 */
public class CustomSynonymeLoader extends CsvLoader {

	public static void main(String[] args) {
		DatabaseConnection.runSession(session -> {
			try {
				CustomSynonymeLoader loader = new CustomSynonymeLoader(session);
				loader.execute();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	private static final String SUBSTANCE_TYPE = "S";
	private static final String PRODUCT_TYPE = "P";

	public static String resolveLabel(String type) {
		return switch (type) {
			case SUBSTANCE_TYPE -> SUBSTANCE_LABEL;
			case PRODUCT_TYPE -> PRODUCT_LABEL;
			default -> null;
		};
	}

	private static final String TYPE = "Type";
	private static final String MMI_ID = "MmiId";
	private static final String SYNONYME = "Synonyme";

	public CustomSynonymeLoader(Session session) throws IOException {
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
