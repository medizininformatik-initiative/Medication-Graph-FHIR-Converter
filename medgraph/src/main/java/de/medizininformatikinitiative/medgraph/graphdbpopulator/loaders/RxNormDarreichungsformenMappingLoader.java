package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.nio.file.Path;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;

/**
 * Loads the EDQM to RxNorm dose form mapping from "DarreichungsformenMapping.csv".
 * This loader uses Markus' RxNorm mapping (column 5) instead of Lucy's mapping (column 6).
 * 
 * CSV structure:
 * - Column 0: EDQM Code (PDF-xxxxx)
 * - Column 4: EDQM Dose Form Name
 * - Column 5: RxNorm Mapping (Markus' mapping)
 * - Column 6: RxNorm Mapping Lucy (not used by this loader)
 *
 * @author Lucy
 */
public class RxNormDarreichungsformenMappingLoader extends CsvLoader {

    // Column indices for "DarreichungsformenMapping.csv"
    private static final int COLUMN_EDQM_CODE = 0;
    private static final int COLUMN_EDQM_DOSE_FORM_NAME = 4;
    private static final int COLUMN_RXNORM_MAPPING_MARKUS = 5;

    private static final String DEFAULT_MAPPING_FILE = "DarreichungsformenMapping.csv";
    private static final String OVERRIDE_PROPERTY = "medgraph.rxNormDarreichungsformenMappingFile";
    private static final String OVERRIDE_ENV = "MEDGRAPH_RXNORM_DARREICHUNGSFORMEN_MAPPING_FILE";

    public RxNormDarreichungsformenMappingLoader(Session session) {
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
        // Create constraint for the mapping table (if not already exists)
        executeQuery(
            "CREATE CONSTRAINT edqmRxNormMappingConstraint IF NOT EXISTS " +
            "FOR (m:" + EDQM_RXNORM_MAPPING_LABEL + ") " +
            "REQUIRE m.edqmDoseForm IS UNIQUE"
        );

        // Load the mapping data idempotently (MERGE by edqmDoseForm)
        // Note: Using withHeaders=false because column 4 has no header name
        // Skip the first row (header row) by checking if column 0 starts with "EDQM"
        executeQuery(withLoadStatement(
            "WITH " + ROW_IDENTIFIER + " WHERE " +
                "NOT " + row(COLUMN_EDQM_CODE) + " STARTS WITH 'EDQM' AND " +
                nullIfBlank(row(COLUMN_EDQM_DOSE_FORM_NAME)) + " IS NOT NULL AND " +
                nullIfBlank(row(COLUMN_RXNORM_MAPPING_MARKUS)) + " IS NOT NULL " +
            "MERGE (m:" + EDQM_RXNORM_MAPPING_LABEL + " {edqmDoseForm: " + nullIfBlank(row(COLUMN_EDQM_DOSE_FORM_NAME)) + "}) " +
            "ON CREATE SET " +
                "m.rxnormDoseForm = " + nullIfBlank(row(COLUMN_RXNORM_MAPPING_MARKUS)) + " " +
            "ON MATCH SET " +
                // Overwrite with Markus' mapping values
                "m.rxnormDoseForm = " + nullIfBlank(row(COLUMN_RXNORM_MAPPING_MARKUS)) + "",
            ',',
            false  // Load without headers, use column indices
        ));

        // Create relationships to existing EDQM nodes (case-insensitive by name)
        executeQuery(withLoadStatement(
            "WITH " + ROW_IDENTIFIER + " WHERE " +
                "NOT " + row(COLUMN_EDQM_CODE) + " STARTS WITH 'EDQM' AND " +
                nullIfBlank(row(COLUMN_EDQM_DOSE_FORM_NAME)) + " IS NOT NULL AND " +
                nullIfBlank(row(COLUMN_RXNORM_MAPPING_MARKUS)) + " IS NOT NULL " +
            "MATCH (e:" + EDQM_LABEL + ") WHERE toLower(e.name) = toLower(" + nullIfBlank(row(COLUMN_EDQM_DOSE_FORM_NAME)) + ") " +
            "MERGE (m:" + EDQM_RXNORM_MAPPING_LABEL + " {edqmDoseForm: " + nullIfBlank(row(COLUMN_EDQM_DOSE_FORM_NAME)) + "}) " +
            "MERGE (e)-[:" + EDQM_HAS_RXNORM_MAPPING + "]->(m)",
            ',',
            false  // Load without headers, use column indices
        ));
    }
}

