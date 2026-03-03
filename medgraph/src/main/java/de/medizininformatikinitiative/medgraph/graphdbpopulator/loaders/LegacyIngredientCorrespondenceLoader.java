package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions;
import de.medizininformatikinitiative.medgraph.common.logging.Level;
import de.medizininformatikinitiative.medgraph.common.logging.LogManager;
import de.medizininformatikinitiative.medgraph.common.logging.Logger;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;

/**
 * Modifies nodes labeled with {@link DatabaseDefinitions#MMI_CORRESPONDING_INGREDIENT_LABEL} to be connected via
 * {@link DatabaseDefinitions#INGREDIENT_CORRESPONDS_TO_LABEL} relationships to their base ingredients instead of being
 * connected to the drug directly.
 *
 * @author Markus Budeus
 */
public class LegacyIngredientCorrespondenceLoader extends Loader {

	private final Logger logger = LogManager.getLogger(LegacyIngredientCorrespondenceLoader.class);

	public LegacyIngredientCorrespondenceLoader(Session session) {
		super(session);
	}

	@Override
	protected void executeLoad() {
		startSubtask("Deleting all ambiguous legacy ingredient correspondences");
		// If a legacy corresponding ingredient, meaning an ingredient marked with the "CORRESPONDING" role
		// is connected to a drug that has multiple active ingredients, it is unclear which ingredient it
		// corresponds to. Thus, we drop those.
		Result result = executeQuery(
				"MATCH (d:" + DRUG_LABEL + ")" +
						"-[:" + DRUG_CONTAINS_INGREDIENT_LABEL + "]->" +
						"(c:" + MMI_CORRESPONDING_INGREDIENT_LABEL + ")" +
						" MATCH (d)-[:" + DRUG_CONTAINS_INGREDIENT_LABEL + "]->(i1:MmiIngredient { isActive: true })" +
						" MATCH (d)-[:" + DRUG_CONTAINS_INGREDIENT_LABEL + "]->(i2:MmiIngredient { isActive: true })" +
						" WHERE i1 <> i2" +
						" DETACH DELETE c"
		);
		int nodesDeleted = result.consume().counters().nodesDeleted();
		if (nodesDeleted > 0) {
			logger.log(Level.WARN, "Removed " + nodesDeleted + " ambiguous legacy ingredient correspondences.");
		}

		startSubtask("Connecting legacy ingredient correspondences to the ingredients they belong to");
		executeQuery(
				"MATCH (d:" + DRUG_LABEL + ")" +
						"-[r:" + DRUG_CONTAINS_INGREDIENT_LABEL + "]->" +
						"(c:" + MMI_CORRESPONDING_INGREDIENT_LABEL + ")" +
						" MATCH (d)-[:" + DRUG_CONTAINS_INGREDIENT_LABEL + "]->(i:MmiIngredient { isActive: true }) " +
						withRowLimit(" WITH c, r, i" +
								" REMOVE c:" + MMI_CORRESPONDING_INGREDIENT_LABEL + ", " +
								"c:" + MMI_INGREDIENT_LABEL + ", " +
								"c.mmiId" +
								" SET c.source = 'COMPOSITIONELEMENT'" +
								" MERGE (i)-[:" + INGREDIENT_CORRESPONDS_TO_LABEL + "]->(c)" +
								" DELETE r")
		);

		startSubtask("Cleaning up");
		result = executeQuery(
				"MATCH (c:" + MMI_CORRESPONDING_INGREDIENT_LABEL + ") " +
						"DETACH DELETE c"
		);
		nodesDeleted = result.consume().counters().nodesDeleted();
		if (nodesDeleted > 0) {
			logger.log(Level.WARN, "Removed " + nodesDeleted + " leftover legacy ingredient correspondences. " +
					"Did their drugs not have any active ingredients?");
		}
	}

}
