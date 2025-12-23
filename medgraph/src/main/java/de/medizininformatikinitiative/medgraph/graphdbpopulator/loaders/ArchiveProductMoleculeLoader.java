package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;

/**
 * This class creates the archived ingredient nodes in the database using the ARCHIVE_PRODUCT_MOLECULE table from the
 * MMI Pharmindex. Since there is no archived variant of ITEM.CSV, this class creates virtual drug nodes if required.
 * Only acts upon product nodes marked as archived. This must be executed after substances, units, and archived products
 * have been loaded.
 *
 * @author Markus Budeus
 */
public class ArchiveProductMoleculeLoader extends CsvLoader {

	// TODO Test

	private static final String PRODUCT_ID = "PRODUCT_ID";
	private static final String MOLECULE_ID = "MOLECULE_ID";
	private static final String MOLECULE_TYPE_CODE = "MOLECULETYPECODE"; // 'A' for active.
	private static final String MASS_FROM = "MASSFROM";
	private static final String MASS_TO = "MASSTO";
	private static final String UNIT_CODE = "MOLECULEUNITCODE";

	public ArchiveProductMoleculeLoader(Session session) {
		super("ARCHIVE_PRODUCT_MOLECULE.CSV", session);
	}

	@Override
	protected void executeLoad() {
		startSubtask("Loading archived ingredients");
		executeQuery(withLoadStatement(
				"CREATE (i:" + MMI_INGREDIENT_LABEL + ":" + INGREDIENT_LABEL + ":Temp {" +
						"productId: " + row(PRODUCT_ID) +
						", substanceId: + " + row(MOLECULE_ID) +
						", isActive: (" + row(MOLECULE_TYPE_CODE) + " = 'A')" +
						", massFrom: " + row(MASS_FROM) +
						", massTo: " + row(MASS_TO) +
						", unitCode: " + row(UNIT_CODE) +
						", " + ARCHIVED_ATTR + ": true })"
		));

		startSubtask("Connecting to substance nodes");
		executeQuery(withRowLimit(
				"MATCH (i:" + MMI_INGREDIENT_LABEL + ":Temp) " +
						"MATCH (s:" + SUBSTANCE_LABEL + " {mmiId: i.substanceId}) " +
						withRowLimit("WITH i, s " +
								"CREATE (i)-[:" + INGREDIENT_IS_SUBSTANCE_LABEL + "]->(s)")
		));

		startSubtask("Removing orphans");
		executeQuery("MATCH (i:" + MMI_INGREDIENT_LABEL + ":Temp) " +
				"WHERE NOT EXISTS (i)-[:" + INGREDIENT_IS_SUBSTANCE_LABEL + "]->(:" + SUBSTANCE_LABEL + ") " +
				withRowLimit("WITH i DELETE (i)"));

		startSubtask("Connecting to unit nodes");
		executeQuery(withRowLimit(
				"MATCH (i:" + MMI_INGREDIENT_LABEL + ":Temp) " +
						"MATCH (u:" + UNIT_LABEL + " {mmiCode: i.unitCode}) " +
						withRowLimit("WITH i, u " +
								"CREATE (i)-[:" + INGREDIENT_HAS_UNIT_LABEL + "]->(u)")
		));

		startSubtask("Removing orphans");
		executeQuery("MATCH (i:" + MMI_INGREDIENT_LABEL + ":Temp) " +
				"WHERE NOT EXISTS (i)-[:" + INGREDIENT_HAS_UNIT_LABEL + "]->(:" + UNIT_LABEL + ") " +
				withRowLimit("WITH i DETACH DELETE (i)"));

		// Product nodes, while being the most filtering, are checked last. This is because otherwise, we would have to
		// clean up the virtual drug nodes if something else does not match.
		startSubtask("Connecting to product nodes");
		executeQuery("MATCH (i:" + MMI_INGREDIENT_LABEL + ":Temp) " +
				"MATCH (p:" + PRODUCT_LABEL + " {" + ARCHIVED_ATTR + ": true }) " +
				withRowLimit("WITH i, p " +
						"CREATE (d:" + DRUG_LABEL + "{ " + VIRTUAL_DRUG_ATTR + ": true }) " +
						"CREATE (p)-[:" + PRODUCT_CONTAINS_DRUG_LABEL + "]->(d)-[:" + DRUG_CONTAINS_INGREDIENT_LABEL + "]->(i)"
				)
		);

		startSubtask("Removing orphans");
		executeQuery("MATCH (i:" + MMI_INGREDIENT_LABEL + ":Temp) " +
				"WHERE NOT EXISTS (:" + DRUG_LABEL + ")-[:" + DRUG_CONTAINS_INGREDIENT_LABEL + "]->(i) " +
				withRowLimit("WITH i DETACH DELETE (i)"));

		startSubtask("Cleaning up");
		executeQuery("MATCH (i:" + MMI_INGREDIENT_LABEL + ":Temp) " +
				withRowLimit("WITH i REMOVE i:Temp, i.productId, i.substanceId, i.unitCode"));
	}
}
