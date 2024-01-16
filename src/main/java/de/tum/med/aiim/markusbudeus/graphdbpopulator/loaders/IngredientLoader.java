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
public class IngredientLoader extends CsvLoader {

	private static final String ID = "ID";
	private static final String MOLECULE_ID = "MOLECULEID";
	private static final String MASS_FROM = "MASSFROM";
	private static final String MASS_TO = "MASSTO";
	private static final String MOLECULE_UNIT_CODE = "MOLECULEUNITCODE";
	private static final String MOLECULE_TYPE_CODE = "MOLECULETYPECODE";

	private static final String ACTIVE_INGREDIENT_TYPE_CODE = "A";

	public IngredientLoader(Session session) throws IOException {
		super("COMPOSITIONELEMENT.CSV", session);
	}

	@Override
	protected void executeLoad() {
		executeQuery(
				"CREATE CONSTRAINT mmiIngredientIdConstraint IF NOT EXISTS FOR (i:" + MMI_INGREDIENT_LABEL + ") REQUIRE i.mmiId IS UNIQUE"
		);

		startSubtask("Loading nodes");
		executeQuery(withLoadStatement(
				"CREATE (i:" + MMI_INGREDIENT_LABEL + ":" + INGREDIENT_LABEL +
						" {mmiId: " + intRow(ID) +
						", massFrom: " + row(MASS_FROM) +
						", massTo: " + row(MASS_TO) +
						", isActive: (" + row(MOLECULE_TYPE_CODE) + " = '" + ACTIVE_INGREDIENT_TYPE_CODE + "')" +
						", substanceId: " + intRow(MOLECULE_ID) +
						", unitCode: " + row(MOLECULE_UNIT_CODE) +
						"}) "
		));

		startSubtask("Connecting to substance nodes");
		executeQuery("MATCH (i:" + MMI_INGREDIENT_LABEL + ") " +
						"MATCH (s:" + SUBSTANCE_LABEL + " {mmiId: i.substanceId}) " +
						"WITH i, s " +
						"CREATE (i)-[:" + INGREDIENT_IS_SUBSTANCE_LABEL + "]->(s) "
		);

		startSubtask("Connecting to unit nodes");
		executeQuery(
				"MATCH (i:" + MMI_INGREDIENT_LABEL + ") " +
						"MATCH (u:" + UNIT_LABEL + " {mmiCode: i.unitCode}) " +
						"WITH i, u " +
						"CREATE (i)-[:" + INGREDIENT_HAS_UNIT_LABEL + "]->(u)"
		);

		startSubtask("Cleaning up");
		executeQuery("MATCH (i:"+MMI_INGREDIENT_LABEL+") " +
				"REMOVE i.substanceId, i.unitCode");
	}
}
