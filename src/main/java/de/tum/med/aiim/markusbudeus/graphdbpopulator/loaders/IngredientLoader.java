package de.tum.med.aiim.markusbudeus.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.io.IOException;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.*;

/**
 * Creates ingredient nodes using the COMPOSITIONELEMENT table from the MMI PharmIndex. Also links them to corresponding
 * substance and unit nodes. Requires the substance and unit nodes to already exist.
 * <p>
 * Includes the ID, MASSFROM and MASSTO as node attributes, named mmiId, massFrom, massTo. Additionally links to the
 * unit via a HAS_UNIT relation.
 */
public class IngredientLoader extends Loader {

	private static final String ID = "ID";
	private static final String MOLECULE_ID = "MOLECULEID";
	private static final String MASS_FROM = "MASSFROM";
	private static final String MASS_TO = "MASSTO";
	private static final String MOLECULE_UNIT_CODE = "MOLECULEUNITCODE";

	public IngredientLoader(Session session) throws IOException {
		super("COMPOSITIONELEMENT.CSV", session);
	}

	@Override
	protected void executeLoad() {
		executeQuery(
				"CREATE CONSTRAINT ingredientMmiIdConstraint IF NOT EXISTS FOR (i:" + INGREDIENT_LABEL + ") REQUIRE i.mmiId IS UNIQUE"
		);

		executeQuery(withLoadStatement(
				"CREATE (i:" + INGREDIENT_LABEL + " {mmiId: " + intRow(ID) + ", massFrom: " + row(
						MASS_FROM) + ", massTo: " + row(MASS_TO) + "}) "
		));

		executeQuery(withLoadStatement(
				"MATCH (i:" + INGREDIENT_LABEL + " {mmiId: " + intRow(ID) + "}) " +
						"MATCH (s:" + SUBSTANCE_LABEL + " {mmiId: " + intRow(MOLECULE_ID) + "}) " +
						"MATCH (u:" + UNIT_LABEL + " {mmiCode: " + row(MOLECULE_UNIT_CODE) + "}) " +
						"WITH i, s, u " +
						"CREATE (i)-[:" + INGREDIENT_IS_SUBSTANCE_LABEL + "]->(s) " +
						"CREATE (i)-[:" + INGREDIENT_HAS_UNIT_LABEL + "]->(u)"
		));
	}
}
