package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.nio.file.Path;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;

/**
 * Creates additional ingredient nodes which correspond to other existing ingredient nodes. Requires the "normal"
 * ingredient nodes, unit nodes and substance nodes to already exist.
 * Loads the COMPOSITIONELEMENTEQUI_extensions.csv file instead of the default MMI Pharmindex one.
 *
 * @author Markus Budeus
 */
public class IngredientCorrespondenceExtensionsLoader extends IngredientCorrespondenceLoader {

	/*
	 * The COMPOSITIONELEMENTEQUI_extensions.csv file was obtained as follows:
	 * We checked out commit b763a4688cf8fc44ca1576454b7faea9f31e2af0, where ingredients marked as CORRESPONDING
	 * are treated like COMPOSITIONELEMENTEQUI and linked to the active ingredient.
	 * (It was later found that these ingredients most of the time did not refer to the active ingredient, therefore
	 * loading incorrect data. The change was reverted.)
	 *
	 * In that branch, we viewed the generated ingredient correspondences manually and step-by-step filtered out
	 * those that were incorrect. At the end, we were left with this query:
     * ---
     * MATCH (i:MmiIngredient)-[:CORRESPONDS_TO]->(ci:Ingredient { source: 'COMPOSITIONELEMENT' })
     * MATCH (i)-[:IS_SUBSTANCE]->(s:Substance)
     * MATCH (ci)-[:IS_SUBSTANCE]->(cs:Substance)
     * WHERE
     * (s.mmiId = 24293 AND cs.mmiId = 24292) OR
     * (s.mmiId = 24123 AND cs.mmiId = 56161) OR
     * (s.mmiId = 346 AND cs.mmiId = 28347) OR
     * (s.mmiId = 24868 AND cs.mmiId = 24165) OR
     * (s.mmiId = 40748 AND cs.mmiId = 47236) OR
     * (s.mmiId = 24938 AND cs.mmiId = 24294) OR
     * (s.mmiId = 51479 AND cs.mmiId = 51479) OR
     * (s.mmiId = 41588 AND cs.mmiId = 41588) OR
     * (s.mmiId = 56971 AND cs.mmiId = 56971) OR
     * (s.mmiId = 22189 AND cs.mmiId = 22189) OR
     * (s.mmiId = 48300 AND cs.mmiId = 48358) OR
     * (s.mmiId = 25027 AND cs.mmiId = 25026) OR
     * (s.mmiId = 27354 AND cs.mmiId = 51076)
     * OPTIONAL MATCH (i)-[:HAS_UNIT]->(u:Unit)
     * OPTIONAL MATCH (ci)-[:HAS_UNIT]->(cu:Unit)
     * // RETURN s.mmiId, i.massFrom, i.massTo, u.print, s.name, ci.source, ci.massFrom, ci.massTo, cu.print, cs.mmiId, cs.name
     * // ORDER BY s.name, i.massFrom
     * RETURN i.mmiId AS COMPOSITIONELEMENTID, cs.mmiId AS EQMOLECULEID, ci.massFrom AS EQMASSFROM, ci.massTo AS EQMASSTO, cu.mmiCode AS EQMOLECULEUNITCODE
     * ---
     * These are the entries subsequently exported into COMPOSITIONELEMENTEQUI_extensions.csv.
	 */

	public IngredientCorrespondenceExtensionsLoader(Session session) {
		super(Path.of("COMPOSITIONELEMENTEQUI_extensions.csv"), session);
	}

}
