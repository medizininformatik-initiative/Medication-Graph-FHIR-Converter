package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.util.List;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;

/**
 * Creates ingredient nodes using the COMPOSITIONELEMENT table from the MMI PharmIndex. Also links them to corresponding
 * substance and unit nodes. Requires the substance and unit nodes to already exist.
 * <p>
 * Includes the ID, MASSFROM and MASSTO as node attributes, named mmiId, massFrom, massTo. Additionally links to the
 * unit via a HAS_UNIT relation.
 *
 * @author Markus Budeus
 */
public class IngredientLoader extends CsvLoader {

	private static final String ID = "ID";
	private static final String MOLECULE_ID = "MOLECULEID";
	private static final String MASS_FROM = "MASSFROM";
	private static final String MASS_TO = "MASSTO";
	private static final String MOLECULE_UNIT_CODE = "MOLECULEUNITCODE";
	private static final String MOLECULE_TYPE_CODE = "MOLECULETYPECODE";

	private static final String ACTIVE_INGREDIENT_TYPE_CODE = "A";
	private static final String CORRESPONDING_INGREDIENT_TYPE_CODE = "R";
	private static final List<String> ACCCEPTABLE_INGREDIENT_TYPE_CODES = List.of("A", "C", "I", "N", "O", "X");

	private final boolean loadCorrespondingIngredients;

	public IngredientLoader(Session session, boolean loadCorrespondingIngredients) {
		super("COMPOSITIONELEMENT.CSV", session);
		this.loadCorrespondingIngredients = loadCorrespondingIngredients;
	}

	public static String getAcceptableIngredientTypeCodesAsCypherTuple() {
		return "[" + ACCCEPTABLE_INGREDIENT_TYPE_CODES.
				stream()
				.map(s -> "'" + s + "'")
				.reduce((s1, s2) -> s1 + ", " + s2)
				.orElseThrow()
				+ "]";
	}

	@Override
	protected void executeLoad() {
		executeQuery(
				"CREATE CONSTRAINT mmiIngredientIdConstraint IF NOT EXISTS FOR (i:" + MMI_INGREDIENT_LABEL + ") REQUIRE i.mmiId IS UNIQUE"
		);

		startSubtask("Loading nodes");
		executeQuery(withLoadStatement(
				"WITH " + ROW_IDENTIFIER + " WHERE " + row(
						MOLECULE_TYPE_CODE) + " IN " + getAcceptableIngredientTypeCodesAsCypherTuple() +
						" CREATE (i:" + MMI_INGREDIENT_LABEL + ":" + INGREDIENT_LABEL +
						" {mmiId: " + intRow(ID) +
						", massFrom: " + row(MASS_FROM) +
						", massTo: " + row(MASS_TO) +
						", isActive: (" + row(MOLECULE_TYPE_CODE) + " = '" + ACTIVE_INGREDIENT_TYPE_CODE + "')" +
						", substanceId: " + intRow(MOLECULE_ID) +
						", unitCode: " + row(MOLECULE_UNIT_CODE) +
						", " + ARCHIVED_ATTR + ": false " +
						"}) "
		));

		if (loadCorrespondingIngredients) {
			executeQuery(withLoadStatement(
					"WITH " + ROW_IDENTIFIER + " WHERE " + row(
							MOLECULE_TYPE_CODE) + " = '" + CORRESPONDING_INGREDIENT_TYPE_CODE + "'" +
							" CREATE (i:" + MMI_CORRESPONDING_INGREDIENT_LABEL + ":" + MMI_INGREDIENT_LABEL + ":" + INGREDIENT_LABEL +
							" {mmiId: " + row(ID) +
							", massFrom: " + row(MASS_FROM) +
							", massTo: " + row(MASS_TO) +
							", substanceId: " + intRow(MOLECULE_ID) +
							", unitCode: " + row(MOLECULE_UNIT_CODE) +
							"}) "
			));
		}

		startSubtask("Connecting to substance nodes");
		executeQuery("MATCH (i:" + MMI_INGREDIENT_LABEL + ") " +
				"MATCH (s:" + SUBSTANCE_LABEL + " {mmiId: i.substanceId}) " +
				withRowLimit("WITH i, s " +
						"CREATE (i)-[:" + INGREDIENT_IS_SUBSTANCE_LABEL + "]->(s) ")
		);

		startSubtask("Connecting to unit nodes");
		executeQuery(
				"MATCH (i:" + MMI_INGREDIENT_LABEL + ") " +
						"MATCH (u:" + UNIT_LABEL + " {mmiCode: i.unitCode}) " +
						withRowLimit("WITH i, u " +
								"CREATE (i)-[:" + INGREDIENT_HAS_UNIT_LABEL + "]->(u)")
		);

		startSubtask("Cleaning up");
		executeQuery("MATCH (i:" + MMI_INGREDIENT_LABEL + ") " +
				withRowLimit("WITH i REMOVE i.substanceId, i.unitCode"));
	}
}
