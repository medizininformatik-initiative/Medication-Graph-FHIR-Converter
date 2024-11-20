package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import de.medizininformatikinitiative.medgraph.common.logging.Level;
import de.medizininformatikinitiative.medgraph.common.logging.LogManager;
import de.medizininformatikinitiative.medgraph.common.logging.Logger;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Record;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;

/**
 * Creates additional ingredient nodes which correspond to other existing ingredient nodes. Requires the "normal"
 * ingredient nodes, unit nodes and substance nodes to already exist.
 *
 * @author Markus Budeus
 */
public class IngredientCorrespondenceLoader extends CsvLoader {

	private static final Logger logger = LogManager.getLogger(IngredientCorrespondenceLoader.class);

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

		startSubtask("Connecting nodes");
		executeQuery(
				"MATCH (i:" + CORRESPONDING_INGREDIENT_LABEL + ") " +
						"MATCH (p:" + MMI_INGREDIENT_LABEL + " {mmiId: i.parentId}) " +
						"MATCH (s:" + SUBSTANCE_LABEL + " {mmiId: i.moleculeId}) " +
						"MATCH (u:" + UNIT_LABEL + " {mmiCode: i.unitCode}) " +
						withRowLimit("WITH i, p, s, u " +
								"CREATE (p)-[:" + INGREDIENT_CORRESPONDS_TO_LABEL + " {primary: true}]->(i) " +
								"CREATE (i)-[:" + INGREDIENT_IS_SUBSTANCE_LABEL + "]->(s) " +
								"CREATE (i)-[:" + INGREDIENT_HAS_UNIT_LABEL + "]->(u) " +
								"REMOVE i.parentId " +
								"REMOVE i.moleculeId " +
								"REMOVE i.unitCode ")
		);

		startSubtask("Identifying primary correspondences");
		// Set primary = false for all corresponding ingredients where another one with a lower massFrom exists.
		executeQuery(
				"MATCH (i:" + MMI_INGREDIENT_LABEL + ")-[r1:" + INGREDIENT_CORRESPONDS_TO_LABEL + "]->(i1:" + CORRESPONDING_INGREDIENT_LABEL + ") " +
						"MATCH (i)-[r2:" + INGREDIENT_CORRESPONDS_TO_LABEL + "]->(i2:" + CORRESPONDING_INGREDIENT_LABEL + ") " +
						"WHERE i1 <> i2 AND toFloat(replace(i1.massFrom, \",\", \".\")) > toFloat(replace(i2.massFrom, \",\", \".\"))" +
						"SET r1.primary = false"
		);

		startSubtask("Searching for ingredients with multiple correspondences with the same mass");
		executeQuery(
				"MATCH (i:" + MMI_INGREDIENT_LABEL + ")-[r1:" + INGREDIENT_CORRESPONDS_TO_LABEL + " {primary: true}]->(i1:" + CORRESPONDING_INGREDIENT_LABEL + ") " +
						"MATCH (i)-[r2:" + INGREDIENT_CORRESPONDS_TO_LABEL + " {primary: true}]->(i2:" + CORRESPONDING_INGREDIENT_LABEL + ") " +
						"WHERE id(i1) > id(i2)" +
						"WITH i " +
						"MATCH (i)-[r:" + INGREDIENT_CORRESPONDS_TO_LABEL + "]->(i1:" + CORRESPONDING_INGREDIENT_LABEL + ") " +
						"REMOVE r.primary"
		);

		startSubtask("Verifying correspondence units match when multiple correspondences are present");
		executeQuery(
				"MATCH (i:" + MMI_INGREDIENT_LABEL + ")-[r1:" + INGREDIENT_CORRESPONDS_TO_LABEL + "]->(i1:" + CORRESPONDING_INGREDIENT_LABEL + ") " +
						"MATCH (i)-[r2:" + INGREDIENT_CORRESPONDS_TO_LABEL + "]->(i2:" + CORRESPONDING_INGREDIENT_LABEL + ") " +
						"WHERE id(i1) > id(i2) " +
						"MATCH (i1)-[:" + INGREDIENT_HAS_UNIT_LABEL + "]->(u1:" + UNIT_LABEL + ") " +
						"MATCH (i2)-[:" + INGREDIENT_HAS_UNIT_LABEL + "]->(u2:" + UNIT_LABEL + ") " +
						"WHERE u1.name <> u2.name " +
						"WITH i " +
						"MATCH (i)-[r:" + INGREDIENT_CORRESPONDS_TO_LABEL + "]->(i1:" + CORRESPONDING_INGREDIENT_LABEL + ") " +
						"REMOVE r.primary"
		);

		startSubtask("Searching for ingredients where the primary correspondence is unclear");
		Result result = executeQuery(
				"MATCH (i:" + MMI_INGREDIENT_LABEL + ")-[r:" + INGREDIENT_CORRESPONDS_TO_LABEL + "]->(i1:" + CORRESPONDING_INGREDIENT_LABEL + ") " +
						"WHERE r.primary IS NULL " +
						"RETURN DISTINCT i.mmiId");

		// And warn about them.
		while (result.hasNext()) {
			Record record = result.next();
			logger.log(Level.WARN, "MmiIngredient " + record.get(0).asInt()
					+ " corresponds to multiple ingredients with conflicting units or with the same amount! " +
					"Primary correspondence cannot be determined!");
		}

		startSubtask("Removing correspondences where the primary correspondence is unclear");
		executeQuery(
				"MATCH (i:" + MMI_INGREDIENT_LABEL + ")-[r:" + INGREDIENT_CORRESPONDS_TO_LABEL + "]->(i1:" + CORRESPONDING_INGREDIENT_LABEL + ") " +
						"WHERE r.primary IS NULL " +
						"DELETE r");

		startSubtask("Removing orphans");
		executeQuery("MATCH (i:" + CORRESPONDING_INGREDIENT_LABEL + ") " +
				"WHERE NOT (i)<-[:" + INGREDIENT_CORRESPONDS_TO_LABEL + "]-(:" + MMI_INGREDIENT_LABEL + ") " +
				withRowLimit("WITH i DETACH DELETE i"));

		startSubtask("Cleaning up");
		executeQuery(withRowLimit("MATCH (i:" + CORRESPONDING_INGREDIENT_LABEL + ") " +
				"REMOVE i:" + CORRESPONDING_INGREDIENT_LABEL));
	}

}
