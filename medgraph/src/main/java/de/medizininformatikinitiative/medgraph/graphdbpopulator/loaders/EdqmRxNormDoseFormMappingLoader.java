package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.nio.file.Path;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;

/**
 * Loads the comprehensive EDQM to RxNorm dose form mapping table.
 * This table includes EDQM dose forms, corresponding RxNorm dose forms, and additional
 * classification information like TRAC, RCA, AMEC, and ISIC codes.
 *
 * Uses the "edqm_rxnorm_dose_form_mapping.csv" file.
 *
 * @author Lucy
 */
public class EdqmRxNormDoseFormMappingLoader extends CsvLoader {

    private static final String EDQM_DOSE_FORM = "edqm_dose_form";
    private static final String RXNORM_DOSE_FORM = "rxnorm_dose_form";
    private static final String HAS_TRAC_CODE = "has_trac_code";
    private static final String HAS_TRAC_TERM = "has_trac_term";
    private static final String HAS_RCA_CODE = "has_rca_code";
    private static final String HAS_RCA_TERM = "has_rca_term";
    private static final String HAS_AMEC_CODE = "has_amec_code";
    private static final String HAS_AMEC_TERM = "has_amec_term";
    private static final String HAS_ISIC_CODE = "has_isic_code";
    private static final String HAS_ISIC_TERM = "has_isic_term";

    private static final String DEFAULT_MAPPING_FILE = "edqm_rxnorm_dose_form_mapping.csv";
    private static final String OVERRIDE_PROPERTY = "medgraph.edqmRxNormMappingFile";
    private static final String OVERRIDE_ENV = "MEDGRAPH_EDQM_RXNORM_MAPPING_FILE";

    public EdqmRxNormDoseFormMappingLoader(Session session) {
        super(resolveMappingPath(), session);
    }

    private static Path resolveMappingPath() {
        // 1) System property takes precedence
        String prop = System.getProperty(OVERRIDE_PROPERTY);
        if (prop != null && !prop.isBlank()) {
            return Path.of(prop);
        }
        // 2) Environment variable
        String env = System.getenv(OVERRIDE_ENV);
        if (env != null && !env.isBlank()) {
            return Path.of(env);
        }
        // 3) Fallback to default resource name
        return Path.of(DEFAULT_MAPPING_FILE);
    }

    @Override
    protected void executeLoad() {
        // Create constraint for the mapping table
        executeQuery(
            "CREATE CONSTRAINT edqmRxNormMappingConstraint IF NOT EXISTS " +
            "FOR (m:" + EDQM_RXNORM_MAPPING_LABEL + ") " +
            "REQUIRE m.edqmDoseForm IS UNIQUE"
        );

        // Load the mapping data idempotently (MERGE by edqmDoseForm)
        executeQuery(withLoadStatement(
            "WITH " + ROW_IDENTIFIER + " WHERE " + nullIfBlank(row(EDQM_DOSE_FORM)) + " IS NOT NULL " +
            "MERGE (m:" + EDQM_RXNORM_MAPPING_LABEL + " {edqmDoseForm: " + nullIfBlank(row(EDQM_DOSE_FORM)) + "}) " +
            "ON CREATE SET " +
                "m.rxnormDoseForm = " + nullIfBlank(row(RXNORM_DOSE_FORM)) + ", " +
                "m.tracCode = " + nullIfBlank(row(HAS_TRAC_CODE)) + ", " +
                "m.tracTerm = " + nullIfBlank(row(HAS_TRAC_TERM)) + ", " +
                "m.rcaCode = " + nullIfBlank(row(HAS_RCA_CODE)) + ", " +
                "m.rcaTerm = " + nullIfBlank(row(HAS_RCA_TERM)) + ", " +
                "m.amecCode = " + nullIfBlank(row(HAS_AMEC_CODE)) + ", " +
                "m.amecTerm = " + nullIfBlank(row(HAS_AMEC_TERM)) + ", " +
                "m.isicCode = " + nullIfBlank(row(HAS_ISIC_CODE)) + ", " +
                "m.isicTerm = " + nullIfBlank(row(HAS_ISIC_TERM)) + " " +
            "ON MATCH SET " +
                // Overwrite with latest mapping values
                "m.rxnormDoseForm = " + nullIfBlank(row(RXNORM_DOSE_FORM)) + ", " +
                "m.tracCode = " + nullIfBlank(row(HAS_TRAC_CODE)) + ", " +
                "m.tracTerm = " + nullIfBlank(row(HAS_TRAC_TERM)) + ", " +
                "m.rcaCode = " + nullIfBlank(row(HAS_RCA_CODE)) + ", " +
                "m.rcaTerm = " + nullIfBlank(row(HAS_RCA_TERM)) + ", " +
                "m.amecCode = " + nullIfBlank(row(HAS_AMEC_CODE)) + ", " +
                "m.amecTerm = " + nullIfBlank(row(HAS_AMEC_TERM)) + ", " +
                "m.isicCode = " + nullIfBlank(row(HAS_ISIC_CODE)) + ", " +
                "m.isicTerm = " + nullIfBlank(row(HAS_ISIC_TERM)) + "",
            ','
        ));

        // Create relationships to existing EDQM nodes (case-insensitive by name)
        executeQuery(withLoadStatement(
            "WITH " + ROW_IDENTIFIER + " WHERE " + nullIfBlank(row(EDQM_DOSE_FORM)) + " IS NOT NULL " +
            "MATCH (e:" + EDQM_LABEL + ") WHERE toLower(e.name) = toLower(" + nullIfBlank(row(EDQM_DOSE_FORM)) + ") " +
            "MERGE (m:" + EDQM_RXNORM_MAPPING_LABEL + " {edqmDoseForm: " + nullIfBlank(row(EDQM_DOSE_FORM)) + "}) " +
            "MERGE (e)-[:" + EDQM_HAS_RXNORM_MAPPING + "]->(m)",
            ','
        ));
    }
}
