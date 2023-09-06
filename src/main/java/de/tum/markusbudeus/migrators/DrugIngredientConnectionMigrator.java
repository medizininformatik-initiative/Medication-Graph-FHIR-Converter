package de.tum.markusbudeus.migrators;

import org.neo4j.driver.Query;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.io.IOException;
import java.nio.file.Path;

import static de.tum.markusbudeus.DatabaseDefinitions.*;
import static org.neo4j.driver.Values.parameters;

/**
 * This migrator reads the ITEM_COMPOSITIONELEMENT table and uses it to connect existing Ingredient and Drug nodes.
 */
public class DrugIngredientConnectionMigrator extends Migrator {

	private static final int ITEM_ID_INDEX = 0;
	private static final int COMPOSITION_ELEMENT_ID_INDEX = 1;

	public DrugIngredientConnectionMigrator(Path directory, Session session)
	throws IOException {
		super(directory, "ITEM_COMPOSITIONELEMENT.CSV", session);
	}

	@Override
	public void migrateLine(String[] line) {
		addRelation(
				Long.parseLong(line[ITEM_ID_INDEX]),
				Long.parseLong(line[COMPOSITION_ELEMENT_ID_INDEX])
		);
	}

	private void addRelation(long drugId, long ingredientId) {
		Result result = session.run(new Query(
				"MATCH (d:" + DRUG_LABEL + " {mmi_id: $drug_id}) " +
						"MATCH (i:" + INGREDIENT_LABEL + " {mmi_id: $ingredient_id}) " +
						"CREATE (d)-[r:" + DRUG_CONTAINS_INGREDIENT_LABEL + "]->(i) " +
						"RETURN r",
				parameters("drug_id", drugId, "ingredient_id", ingredientId)
		));

		assertSingleRow(result,
				"Attempting to connect drug " + drugId + " with ingredient " + ingredientId + " resulted in no matches!",
				"Attempting to connect drug " + drugId + " with ingredient " + ingredientId + " resulted in multiple matches!");
	}
}
