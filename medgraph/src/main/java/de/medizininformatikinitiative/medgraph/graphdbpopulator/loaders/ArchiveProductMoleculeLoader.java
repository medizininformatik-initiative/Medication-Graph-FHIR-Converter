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
	// TODO Test case where MMI ID is overloaded

	private static final String TEMP_INGREDIENT_PRODUCT_RELATIONSHIP = "TEMP_ARCHIVE_INGREDIENT_ASSIGNED_TO";

	private static final String PRODUCT_ID = "PRODUCT_ID";
	private static final String MOLECULE_ID = "MOLECULE_ID";
	private static final String MOLECULE_TYPE_CODE = "MOLECULETYPECODE"; // 'A' for active.
	private static final String MASS_FROM = "MASSFROM";
	private static final String MASS_TO = "MASSTO";
	private static final String UNIT_CODE = "MOLECULEUNITCODE";
	private static final String COUNT = "BASECOUNT";
	private static final String COUNT_UNIT_CODE = "BASEMOLECULEUNITCODE";

	public ArchiveProductMoleculeLoader(Session session) {
		super("ARCHIVE_PRODUCTMOLECULE.CSV", session);
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
						", baseCount: " + row(COUNT) +
						", amountUnit: " + row(COUNT_UNIT_CODE) +
						", " + ARCHIVED_ATTR + ": true })"
		));

		// Temporarily connect ingredients to products. There is no concept of a drug in the archive. We create virtual
		// drug nodes later.
		startSubtask("Connecting to product nodes");
		executeQuery("MATCH (i:" + MMI_INGREDIENT_LABEL + ":Temp) " +
				"MATCH (p:" + PRODUCT_LABEL + " {" + ARCHIVED_ATTR + ": true }) " + // Can only connect to archived products.
				withRowLimit("WITH i, p " +
						"CREATE (i)-[:" + TEMP_INGREDIENT_PRODUCT_RELATIONSHIP + " { " +
						"amount: " + row(COUNT) +
						", amountUnit: " + row(COUNT_UNIT_CODE) +
						" })->(p)"
				)
		);

		startSubtask("Removing orphans");
		executeQuery("MATCH (i:" + MMI_INGREDIENT_LABEL + ":Temp) " +
				"WHERE NOT EXISTS (i)-[:" + TEMP_INGREDIENT_PRODUCT_RELATIONSHIP + "]->(:" + PRODUCT_LABEL + ") " +
				withRowLimit("WITH i DELETE i"));

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
				withRowLimit("WITH i DETACH DELETE i"));

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
				withRowLimit("WITH i DETACH DELETE i"));

		startSubtask("Creating virtual drug nodes");
		// The design of this is so strange. Because there is no ARCHIVE_ITEM.CSV, the base counts of items
		// are assigned to ARCHIVE_PRODUCT_MOLECULE.CSV. But since in there, a row specifies an ingredient, a drug with
		// multiple ingredients has one base count definition per ingredient. It seems like these always match, but
		// I did not check the full data set.
		// So I do this: IF the base count and unit matches, the ingredients are assumed to belong to the same drug.
		// Otherwise, they are assumed to belong to different drugs and thus get assigned to different virtual drug
		// nodes.
		// For example, if I have these three ingredients assigned to the same product:
		// | MOLECULEID | BASECOUNT | BASEMOLECULEUNITCODE |
		// |------------|-----------|----------------------|
		// | 1          | 10        | ML                   |
		// | 2          | 10        | ML                   |
		// | 3          | 5         | ML                   |
		// | 4          | 10        | G                    |
		// In this case, substances 1 and two are assigned to one virtual drug node, which gets "10ml" as assigned amount.
		// Substances 3 and 4 each get their own virtual drug node.
		// TODO Test this actually works as intended
		executeQuery("MATCH (i:" + MMI_INGREDIENT_LABEL + ":Temp)" +
				"-[r:" + TEMP_INGREDIENT_PRODUCT_RELATIONSHIP + "]->" +
				"(p:" + PRODUCT_LABEL + ") " +
				withRowLimit("WITH p, r, i " +
						"MERGE (d: " + DRUG_LABEL + ":Temp { "
						+ VIRTUAL_DRUG_ATTR + ": true, " +
						"amount: r.amount" +
						", amountUnit: r.amountUnit" +
						" }) " +
						"MERGE (p)-[:" + PRODUCT_CONTAINS_DRUG_LABEL + "]->(d)" +
						"CREATE (d)-[:" + DRUG_CONTAINS_INGREDIENT_LABEL + "]->(i)", 1500));

		startSubtask("Removing temporary product-ingredient relationships");
		executeQuery("MATCH [r:"+TEMP_INGREDIENT_PRODUCT_RELATIONSHIP+"] " +
				withRowLimit("WITH r DELETE r"));

		startSubtask("Connecting virtual drug nodes to unit nodes");
		executeQuery("MATCH (d:" + DRUG_LABEL + ": Temp) " +
				"MATCH (u:" + UNIT_LABEL + " {mmiCode: i.amountUnit} " +
				withRowLimit("WITH d, u " +
						"CREATE (d)-[:" + DRUG_HAS_UNIT_LABEL + "]->(u)"));

		startSubtask("Cleaning up");
		executeQuery("MATCH (d:" + DRUG_LABEL + ":Temp) " +
				withRowLimit("WITH d REMOVE d:Temp, d.amontUnit"));
		executeQuery("MATCH (i:" + MMI_INGREDIENT_LABEL + ":Temp) " +
				withRowLimit("WITH i REMOVE i:Temp, i.productId, i.substanceId, i.unitCode"));
	}
}
