package de.tum.med.aiim.markusbudeus.graphdbpopulator.migrators;

import org.neo4j.driver.Query;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.io.IOException;
import java.nio.file.Path;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.*;
import static org.neo4j.driver.Values.parameters;

/**
 * Creates ingredient nodes using the COMPOSITIONELEMENT table from the MMI PharmIndex.
 * Also links them to corresponding substance nodes. Requires the substance nodes to already exist.
 * <p>
 * Includes the ID, MASSFROM, MASSTO, MOLECULEUNITCATALOGID and MOLECULEUNITCODE as node attributes, named
 * mmi_id, mass_from, mass_to, molecule_unit_catalog_id and molecule_unit_code, respectively.
 */
public class IngredientMigrator extends Migrator {

	private static final int ID_INDEX = 0;
	private static final int MOLECULE_ID_INDEX = 1;
	private static final int MASS_FROM_INDEX = 4;
	private static final int MASS_TO_INDEX = 5;
	private static final int MOLECULE_UNIT_CATALOG_ID_INDEX = 6;
	private static final int MOLECULE_UNIT_CODE_INDEX = 7;

	public IngredientMigrator(Path directory, Session session)
	throws IOException {
		super(directory, "COMPOSITIONELEMENT.CSV", session);
	}

	@Override
	public void migrateLine(String[] line) {
		addNode(
				Long.parseLong(line[ID_INDEX]),
				line[MASS_FROM_INDEX],
				line[MASS_TO_INDEX],
				Long.parseLong(line[MOLECULE_UNIT_CATALOG_ID_INDEX]),
				line[MOLECULE_UNIT_CODE_INDEX],
				Long.parseLong(line[MOLECULE_ID_INDEX])
		);
	}

	private void addNode(long id, String massFrom, String massTo, long unitCatalogId, String unitCode, long substanceId) {
		Result result = session.run(new Query(
				"CREATE (i:"+INGREDIENT_LABEL+" {mmi_id: $mmi_id, mass_from: $mass_from, mass_to: $mass_to, molecule_unit_catalog_id: $unit_catalog_id, molecule_unit_code: $unit_code}) " +
						"WITH i " +
						"MATCH (s:"+SUBSTANCE_LABEL+" {mmi_id: $substance_id}) " +
						"CREATE (i)-[r:"+INGREDIENT_IS_SUBSTANCE_LABEL+"]->(s) " +
						"RETURN s, r, i",
				parameters("mmi_id", id, "mass_from", massFrom, "mass_to", massTo, "unit_catalog_id", unitCatalogId, "unit_code", unitCode, "substance_id", substanceId)
		));

		assertSingleRow(result,
				"Warning: The ingredient "+id+" could not be matched to a substance!",
				"Warning: The ingredient "+id+" was matched to multiple substances!");
	}

}
