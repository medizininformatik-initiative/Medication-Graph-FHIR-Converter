package de.tum.med.aiim.markusbudeus.graphdbpopulator.loaders;

import de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions;
import org.neo4j.driver.Session;

import java.io.IOException;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.*;

/**
 * This loader reads the ITEM_COMPOSITIONELEMENT table and uses it to connect existing Ingredient and Drug nodes.
 */
public class DrugIngredientConnectionLoader extends CsvLoader {

	private static final String ITEM_ID = "ITEMID";
	private static final String COMPOSITION_ELEMENT_ID = "COMPOSITIONELEMENTID";

	public DrugIngredientConnectionLoader(Session session) throws IOException {
		super("ITEM_COMPOSITIONELEMENT.CSV", session);
	}

	@Override
	protected void executeLoad() {
		executeQuery(withLoadStatement(
				"MATCH (d:" + DRUG_LABEL + " {mmiId: " + intRow(ITEM_ID) + "}) " +
						"MATCH (i:" + MMI_INGREDIENT_LABEL + " {mmiId: " + intRow(
						COMPOSITION_ELEMENT_ID) + "}) " +
						"CREATE (d)-[:" + DRUG_CONTAINS_INGREDIENT_LABEL + "]->(i) "
		));
	}
}
