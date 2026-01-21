package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.searchengine.db.Neo4jCypherDatabase;
import org.jetbrains.annotations.Nullable;
import org.neo4j.driver.Session;

import java.util.Map;

/**
 * Maps dose forms to RxNorm-compatible form strings using data loaded into Neo4j
 * from the CSV ("DarreichungsformenMapping.csv").
 *
 * This class provides a static API for convenience. You MUST initialize it once
 * with a Neo4jCypherDatabase via {@link #initialize(Neo4jCypherDatabase)} before use.
 * No hardcoded or heuristic fallbacks are used.
 */
public final class DoseFormMapper {

    private static Neo4jCypherDatabase database;

    private DoseFormMapper() {}

    /**
     * Initializes the mapper with a database instance. Call once during startup.
     */
    public static void initialize(Neo4jCypherDatabase db) {
        database = db;
    }

    /**
     * Returns the RxNorm-compatible dose form for the given EDQM dose form, or null if none is mapped.
     * Requires {@link #initialize(Neo4jCypherDatabase)} to have been called.
     */
    public static @Nullable String mapEdqm(GraphEdqmPharmaceuticalDoseForm edqm) {
        if (edqm == null) return null;
        if (database == null) {
            throw new IllegalStateException("DoseFormMapper not initialized. Call DoseFormMapper.initialize(database) first.");
        }

        // Use the shared session managed by the caller; do not close it here
        Session session = database.getSession();
            String cypher = """
                MATCH (e:EDQM {code: $edqmCode})-[:HAS_RXNORM_MAPPING]->(m:EdqmRxNormMapping)
                RETURN m.rxnormDoseForm AS rxnormDoseForm
                """;
            var result = session.run(cypher, Map.of("edqmCode", edqm.getCode()));
            if (result.hasNext()) {
                var record = result.next();
                String rx = record.get("rxnormDoseForm").asString(null);
                // Normalize to lowercase to match RxNorm's standard format
                return rx == null || rx.isBlank() ? null : rx.toLowerCase();
            }
            return null;
        
    }

    /**
     * Returns the full mapping node for the given EDQM dose form, or null if none exists.
     */
    public static @Nullable EdqmRxNormDoseFormMapping getEdqmMapping(GraphEdqmPharmaceuticalDoseForm edqm) {
        if (edqm == null) return null;
        if (database == null) {
            throw new IllegalStateException("DoseFormMapper not initialized. Call DoseFormMapper.initialize(database) first.");
        }
        // Use the shared session managed by the caller; do not close it here
        Session session = database.getSession();
            String cypher = """
                MATCH (e:EDQM {code: $edqmCode})-[:HAS_RXNORM_MAPPING]->(m:EdqmRxNormMapping)
                RETURN m AS m
                """;
            var result = session.run(cypher, Map.of("edqmCode", edqm.getCode()));
            if (result.hasNext()) {
                var record = result.next();
                return EdqmRxNormDoseFormMapping.from(record.get("m"));
            }
            return null;
        
    }
}
