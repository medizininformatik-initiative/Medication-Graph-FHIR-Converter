package de.medizininformatikinitiative.medgraph.fhirexporter.exporter;

import de.medizininformatikinitiative.medgraph.common.logging.Level;
import de.medizininformatikinitiative.medgraph.common.logging.LogManager;
import de.medizininformatikinitiative.medgraph.common.logging.Logger;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.Medication;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.substance.Substance;
import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.*;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;

import java.util.stream.Stream;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;

/**
 * This class generates {@link GraphProduct}-objects using the Neo4j knowledge graph.
 *
 * @author Markus Budeus
 */
public class Neo4jProductExporter extends Neo4jExporter<GraphProduct> {

	private static final Logger logger = LogManager.getLogger(Neo4jProductExporter.class);

	private final boolean allowMedicationsWithoutIngredients;

	/**
	 * Instantiates a new exporter.
	 *
	 * @param session                            the database session to use for querying the knowledge graph
	 * @param allowMedicationsWithoutIngredients if true, {@link Medication} instances are created even for products for
	 *                                           which no ingredients are known. Note that these instances are no longer
	 *                                           compliant to the 2023 MII FHIR profile, which requires at least one
	 *                                           ingredient be specified.
	 */
	public Neo4jProductExporter(Session session, boolean allowMedicationsWithoutIngredients) {
		super(session);
		this.allowMedicationsWithoutIngredients = allowMedicationsWithoutIngredients;
	}

	/**
	 * Reads all medications with their assigned codes and coding systems as well as ingredients and dose form from the
	 * database and returns them as a stream of {@link Substance Substances}.
	 */
    @Override
    public Stream<GraphProduct> exportObjects() {
        final int pageSize = 200;
        final Integer productLimit = parseProductLimit();
        java.util.List<GraphProduct> all = new java.util.ArrayList<>();
        int skip = 0;
        while (true) {
            java.util.List<Long> ids = session.run(
                    "MATCH (p:" + PRODUCT_LABEL + ") RETURN p.mmiId AS id ORDER BY p.mmiId SKIP $skip LIMIT $limit",
                    Values.parameters("skip", skip, "limit", pageSize)
            ).list(r -> r.get("id").asLong());
            if (ids.isEmpty()) break;
    // This is a complicated query. Sorry about that. :(
            String query =
                    "MATCH (p:" + PRODUCT_LABEL + ")-[:" + PRODUCT_CONTAINS_DRUG_LABEL + "]->(d:" + DRUG_LABEL + ") " +
                            "WHERE p.mmiId IN $ids " +

                            "CALL {" +
                            "    WITH d" +
                            "    OPTIONAL MATCH (d)-[:" + DRUG_HAS_DOSE_FORM_LABEL + "]->(df:" + MMI_DOSE_FORM_LABEL + ") " +
                            "    OPTIONAL MATCH (df)-[:" + DOSE_FORM_IS_EDQM + "]->(de:" + EDQM_LABEL + ")-[:" + BELONGS_TO_CODING_SYSTEM_LABEL + "]->(dfcs:" + CODING_SYSTEM_LABEL + ") " +
                            "    RETURN df, " + GraphUtil.groupCodingSystem("de", "dfcs",
                            GraphEdqmPharmaceuticalDoseForm.NAME + ":de.name") + " AS edqmDoseForm" +
                            "}" +

                            "CALL {" +
                            "    WITH d" +
                            "    OPTIONAL MATCH (d)-[:" + DRUG_CONTAINS_INGREDIENT_LABEL + "]->(i:" + MMI_INGREDIENT_LABEL + ")-[:" + INGREDIENT_IS_SUBSTANCE_LABEL + "]->(s:" + SUBSTANCE_LABEL + ") " +
                            "    OPTIONAL MATCH (rx:" + RXCUI_LABEL + ")-[:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]->(s) " +
                            "    OPTIONAL MATCH (i)-[:" + INGREDIENT_CORRESPONDS_TO_LABEL + "]->(ci:" + INGREDIENT_LABEL + ")-[:" + INGREDIENT_IS_SUBSTANCE_LABEL + "]->(cis:" + SUBSTANCE_LABEL + ") " +
                            "    OPTIONAL MATCH (ci)-[:" + INGREDIENT_HAS_UNIT_LABEL + "]->(ciu:" + UNIT_LABEL + ") " +
                            "    OPTIONAL MATCH (i)-[:" + INGREDIENT_HAS_UNIT_LABEL + "]->(iu:" + UNIT_LABEL + ") " +
                            "    WITH i, s, iu, collect(DISTINCT rx.code) AS rxCodes, " +
                            "    collect(CASE WHEN ci IS NOT NULL THEN {" +
                            SimpleGraphIngredient.SUBSTANCE_MMI_ID + ":cis.mmiId," +
                            SimpleGraphIngredient.SUBSTANCE_NAME + ":cis.name," +
                            SimpleGraphIngredient.MASS_FROM + ":ci.massFrom," +
                            SimpleGraphIngredient.MASS_TO + ":ci.massTo," +
                            SimpleGraphIngredient.UNIT + ":ciu" +
                            "} ELSE null END) AS corresponding " +
                            "    RETURN collect(CASE WHEN s IS NOT NULL THEN {" +
                            SimpleGraphIngredient.SUBSTANCE_MMI_ID + ":s.mmiId," +
                            SimpleGraphIngredient.SUBSTANCE_NAME + ":s.name," +
                            GraphIngredient.IS_ACTIVE + ":i.isActive," +
                            SimpleGraphIngredient.MASS_FROM + ":i.massFrom," +
                            SimpleGraphIngredient.MASS_TO + ":i.massTo," +
                            SimpleGraphIngredient.UNIT + ":iu," +
                            SimpleGraphIngredient.RXCUI_CODES + ":rxCodes," +
                            GraphIngredient.CORRESPONDING_INGREDIENTS + ":corresponding" +
                            "} ELSE null END) AS ingredients" +
                            "}" +

                            "OPTIONAL MATCH (d)-[:" + DRUG_MATCHES_ATC_CODE_LABEL + "]->(a:" + ATC_LABEL + ")-[:" + BELONGS_TO_CODING_SYSTEM_LABEL + "]->(acs:" + CODING_SYSTEM_LABEL + ") " +
                            "OPTIONAL MATCH (d)-[:" + DRUG_HAS_UNIT_LABEL + "]->(du:" + UNIT_LABEL + ") " +
                            "WITH p, d, df, edqmDoseForm, ingredients, " +
                            "collect(" + GraphUtil.groupCodingSystem("a", "acs", GraphAtc.DESCRIPTION + ":a.description") +
                            ") AS atcCodes, du " +

                            "OPTIONAL MATCH (p)<-[:" + PACKAGE_BELONGS_TO_PRODUCT_LABEL + "]-(pk:" + PACKAGE_LABEL + ") " +
                            "OPTIONAL MATCH (pkcs:" + CODING_SYSTEM_LABEL + ")<-[:" + BELONGS_TO_CODING_SYSTEM_LABEL + "]-(pkc:" + CODE_LABEL + ")-->(pk) " +

                            "WITH p, d, df, edqmDoseForm, ingredients, atcCodes, du, pk, " +
                            "collect(" + GraphUtil.groupCodingSystem("pkc", "pkcs") + ") as packageCodes " +
                            "WITH p, d, df, edqmDoseForm, ingredients, atcCodes, du, " +
                            "collect({" +
                            GraphPackage.NAME + ":pk.name," +
                            GraphPackage.AMOUNT + ":pk.amount," +
                            GraphPackage.ON_MARKET_DATE + ":pk.onMarketDate," +
                            GraphPackage.CODES + ":packageCodes" +
                            "}) as packages " +
                            "OPTIONAL MATCH (pcs:" + CODING_SYSTEM_LABEL + ")<-[:" + BELONGS_TO_CODING_SYSTEM_LABEL + "]-(pc:" + CODE_LABEL + ")-->(p) " +

                            "OPTIONAL MATCH (c:" + COMPANY_LABEL + ")-[:" + MANUFACTURES_LABEL + "]->(p) " +
                            
                            "RETURN p.name AS " + GraphProduct.PRODUCT_NAME + "," +
                            "p.mmiId AS " + GraphProduct.MMI_ID + "," +
                            "c.mmiId AS " + GraphProduct.COMPANY_MMI_ID + "," +
                            "c.name AS " + GraphProduct.COMPANY_NAME + "," +
                            "collect(" + GraphUtil.groupCodingSystem("pc", "pcs") + ") AS " + GraphProduct.PRODUCT_CODES + ", " +
                            "collect({" +
                            GraphDrug.INGREDIENTS + ":ingredients," +
                            GraphDrug.ATC_CODES + ":atcCodes," +
                            GraphDrug.MMI_DOSE_FORM + ":df.mmiName," +
                            GraphDrug.EDQM_DOSE_FORM + ":edqmDoseForm," +
                            GraphDrug.AMOUNT + ":d.amount," +
                            GraphDrug.UNIT + ":du" +
                            "}) AS " + GraphProduct.DRUGS + ", " +
                            "packages AS " + GraphProduct.PACKAGES;

            logger.log(Level.DEBUG, "Medication Graph DB Query: " + query);
            all.addAll(session.run(query, Values.parameters("ids", ids)).stream().map(GraphProduct::new).toList());
            skip += ids.size();
            if (productLimit != null && all.size() >= productLimit) {
                return all.stream().limit(productLimit);
            }
        }
        return all.stream();
    }

    private Integer parseProductLimit() {
        String prop = System.getProperty("medgraph.export.productLimit");
        if (prop == null) return null;
        try {
            int val = Integer.parseInt(prop);
            return val > 0 ? val : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

}
