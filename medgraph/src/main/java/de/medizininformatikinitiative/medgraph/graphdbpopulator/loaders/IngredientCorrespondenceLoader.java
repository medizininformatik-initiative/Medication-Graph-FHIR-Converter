package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;

/**
 * Creates additional ingredient nodes which correspond to other existing ingredient nodes. Requires the "normal"
 * ingredient nodes, unit nodes and substance nodes to already exist.
 *
 * @author Markus Budeus
 */
public class IngredientCorrespondenceLoader extends CsvLoader {

	private static final String CORRESPONDING_INGREDIENT_LABEL = "CorrespondingIngredient";

	private static final String COMPOSITION_ELEMENT_ID = "COMPOSITIONELEMENTID";
	private static final String CORRESPONDING_MOLECULE_ID = "EQMOLECULEID";
	private static final String MASS_FROM = "EQMASSFROM";
	private static final String MASS_TO = "EQMASSTO";
	private static final String MOLECULE_UNIT_CODE = "EQMOLECULEUNITCODE";

	public IngredientCorrespondenceLoader(Session session) {
		super("COMPOSITIONELEMENTEQUI.CSV", session);
	}

	@Override
	protected void executeLoad() {

		startSubtask("Loading nodes");
		executeQuery(withLoadStatement(
				"CREATE (i:" + INGREDIENT_LABEL + ":" + CORRESPONDING_INGREDIENT_LABEL + " {parentId: " + intRow(
						COMPOSITION_ELEMENT_ID)
						+ ", moleculeId: " + intRow(CORRESPONDING_MOLECULE_ID)
						+ ", unitCode: " + row(MOLECULE_UNIT_CODE)
						+ ", massFrom: " + row(MASS_FROM)
						+ ", massTo: " + row(MASS_TO) + "}) "
		));


		startSubtask("Connecting to ingredients");
		executeQuery(
				"MATCH (i:" + CORRESPONDING_INGREDIENT_LABEL + ") " +
						"MATCH (p:" + MMI_INGREDIENT_LABEL + " {mmiId: i.parentId}) " +
						withRowLimit("WITH i, p, s, u " +
								"CREATE (p)-[:" + INGREDIENT_CORRESPONDS_TO_LABEL + "]->(i) " +
								"REMOVE i.parentId ")
		);

		startSubtask("Removing orphans");
		executeQuery("MATCH (i:" + CORRESPONDING_INGREDIENT_LABEL + ") " +
				"WHERE NOT (i)<-[:" + INGREDIENT_CORRESPONDS_TO_LABEL + "]-(:" + MMI_INGREDIENT_LABEL + ") " +
				withRowLimit("WITH i DELETE i"));

		startSubtask("Connecting remaining nodes");
		executeQuery(
				"MATCH (i:" + CORRESPONDING_INGREDIENT_LABEL + ") " +
						"MATCH (s:" + SUBSTANCE_LABEL + " {mmiId: i.moleculeId}) " +
						"MATCH (u:" + UNIT_LABEL + " {mmiCode: i.unitCode}) " +
						withRowLimit("WITH i, p, s, u " +
								"CREATE (i)-[:" + INGREDIENT_IS_SUBSTANCE_LABEL + "]->(s) " +
								"CREATE (i)-[:" + INGREDIENT_HAS_UNIT_LABEL + "]->(u) " +
								"REMOVE i.moleculeId " +
								"REMOVE i.unitCode ")
		);

		startSubtask("Cleaning up");
		executeQuery(withRowLimit("MATCH (i:" + CORRESPONDING_INGREDIENT_LABEL + ") " +
				"REMOVE i:" + CORRESPONDING_INGREDIENT_LABEL));
	}

}
