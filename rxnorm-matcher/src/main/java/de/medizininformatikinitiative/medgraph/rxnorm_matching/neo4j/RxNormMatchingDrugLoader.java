package de.medizininformatikinitiative.medgraph.rxnorm_matching.neo4j;

import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.GraphEdqmPharmaceuticalDoseForm;
import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.GraphUtil;
import de.medizininformatikinitiative.medgraph.rxnorm_matching.neo4j.model.ActiveIngredient;
import de.medizininformatikinitiative.medgraph.rxnorm_matching.neo4j.model.Drug;
import de.medizininformatikinitiative.medgraph.rxnorm_matching.neo4j.model.SimpleActiveIngredient;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;

import java.util.List;
import java.util.stream.Stream;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;

/**
 * Provides the query for extracting the drugs for RxNorm Matching.
 *
 * @author Markus Budeus
 */
public class RxNormMatchingDrugLoader {

	private final Session session;

	public RxNormMatchingDrugLoader(Session session) {
		this.session = session;
	}

	public Stream<Drug> loadDrugs(List<Integer> productMmiIdFilter) {

		String query =
				"MATCH (p:" + PRODUCT_LABEL + ")-[:" + PRODUCT_CONTAINS_DRUG_LABEL + "]->(d:" + DRUG_LABEL + ") " +
						(productMmiIdFilter != null ? "WHERE p.mmiId IN $ids " : "") +
						"CALL {" +
						"    WITH d" +
						"    OPTIONAL MATCH (d)-[:" + DRUG_HAS_DOSE_FORM_LABEL + "]->(df:" + MMI_DOSE_FORM_LABEL + ") " +
						"    OPTIONAL MATCH (df)-[:" + DOSE_FORM_IS_EDQM + "]->(de:" + EDQM_LABEL + ")-[:" + BELONGS_TO_CODING_SYSTEM_LABEL + "]->(dfcs:" + CODING_SYSTEM_LABEL + ") " +
						"    RETURN df, " + GraphUtil.groupCodingSystem("de", "dfcs",
						GraphEdqmPharmaceuticalDoseForm.NAME + ":de.name") + " AS edqmDoseForm" +
						"}" +
						"CALL {" +
						"    WITH d" +
						"    OPTIONAL MATCH (d)-[:" + DRUG_CONTAINS_INGREDIENT_LABEL + "]->(i:" + MMI_INGREDIENT_LABEL + " { isActive: true })-[:" + INGREDIENT_IS_SUBSTANCE_LABEL + "]->(s:" + SUBSTANCE_LABEL + ") " +
						"    OPTIONAL MATCH (i)-[:" + INGREDIENT_CORRESPONDS_TO_LABEL + "]->(ci:" + INGREDIENT_LABEL + ")-[:" + INGREDIENT_IS_SUBSTANCE_LABEL + "]->(cis:" + SUBSTANCE_LABEL + ") " +
						"    CALL {" +
						"        WITH cis" +
						"        OPTIONAL MATCH (cis)<-[:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]-(rx:" + RXCUI_LABEL + ")" +
						"        RETURN collect(DISTINCT rx.code) as cisRxcui" +
						"    }" +
						"    OPTIONAL MATCH (ci)-[:" + INGREDIENT_HAS_UNIT_LABEL + "]->(ciu:" + UNIT_LABEL + ") " +
						"    OPTIONAL MATCH (i)-[:" + INGREDIENT_HAS_UNIT_LABEL + "]->(iu:" + UNIT_LABEL + ") " +
						"    WITH i, s, iu, " +
						"    collect(CASE WHEN ci IS NOT NULL THEN {" +
						SimpleActiveIngredient.SUBSTANCE_MMI_ID + ":cis.mmiId," +
						SimpleActiveIngredient.SUBSTANCE_NAME + ":cis.name," +
						SimpleActiveIngredient.MASS_FROM + ":ci.massFrom," +
						SimpleActiveIngredient.MASS_TO + ":ci.massTo," +
						SimpleActiveIngredient.UNIT + ":ciu, " +
						SimpleActiveIngredient.RXCUI_CODES + ":cisRxcui" +
						"} ELSE null END) AS corresponding " +
						"OPTIONAL MATCH (rx:" + RXCUI_LABEL + ")-[:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]->(s) " +
						"WITH i, s, iu, corresponding, collect(DISTINCT rx.code) AS rxCodes " +
						"RETURN collect(CASE WHEN s IS NOT NULL THEN {" +
						SimpleActiveIngredient.SUBSTANCE_MMI_ID + ":s.mmiId," +
						SimpleActiveIngredient.SUBSTANCE_NAME + ":s.name," +
						SimpleActiveIngredient.MASS_FROM + ":i.massFrom," +
						SimpleActiveIngredient.MASS_TO + ":i.massTo," +
						SimpleActiveIngredient.UNIT + ":iu," +
						SimpleActiveIngredient.RXCUI_CODES + ":rxCodes," +
						ActiveIngredient.CORRESPONDING_INGREDIENTS + ":corresponding" +
						"} ELSE null END) AS ingredients" +
						"}" +
						"OPTIONAL MATCH (d)-[:" + DRUG_HAS_UNIT_LABEL + "]->(du:" + UNIT_LABEL + ") " +
						"RETURN p.name AS " + Drug.PRODUCT_NAME + "," +
						"p.mmiId AS " + Drug.PRODUCT_MMI_ID + "," +
						"d.mmiId AS " + Drug.DRUG_MMI_ID + ", " +
						"ingredients AS " + Drug.INGREDIENTS + ", " +
						"df.mmiName AS " + Drug.MMI_DOSE_FORM + ", " +
						"edqmDoseForm AS " + Drug.EDQM_DOSE_FORM + ", " +
						"d.amount AS " + Drug.AMOUNT + ", " +
						"du AS " + Drug.UNIT + " " +
						"ORDER BY p.mmiId"; // Order for reproducibility

//		System.out.println(query);

		Result result;
		if (productMmiIdFilter != null) {
			result = session.run(query, Values.parameters("ids", productMmiIdFilter));
		} else {
			result = session.run(query);
		}

		return result.stream().map(Drug::new);
	}


}
