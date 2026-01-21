package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.searchengine.db.Neo4jCypherDatabase;

/**
 * Utility class to set up RxNormProductMatcher with API-based providers.
 * 
 * This class provides convenient methods to initialize the matcher with
 * RxNav API-based implementations for candidate generation and TTY resolution.
 */
public final class RxNormMatcherSetup {

    private RxNormMatcherSetup() {}

    private static volatile RxNormProductMatcher sharedMatcher;

    /**
     * Initializes RxNormProductMatcher with API-based providers.
     * 
     * This method sets up:
     * - RxNavCandidateProvider for SCD/SBD candidate generation
     * - RxNavTtyResolver for IN/PIN selection
     * - DoseFormMapper with the provided Neo4j database
     * 
     * @param neo4jDatabase The Neo4j database instance for dose form mapping
     */
    public static void initializeWithApiProviders(Neo4jCypherDatabase neo4jDatabase) {
        // Initialize dose form mapper
        DoseFormMapper.initialize(neo4jDatabase);
        
        // Set up TTY resolver for IN/PIN selection
        RxNormProductMatcher.setRxcuiTermTypeResolver(new RxNavTtyResolver());
        
        // Set up candidate provider for SCD/SBD generation
        RxNormProductMatcher.setCandidateProvider(new RxNavCandidateProvider());

        // Prepare a shared matcher for export use
        sharedMatcher = new RxNormProductMatcher();
    }

    /**
     * Initializes RxNormProductMatcher with local SQLite-backed providers.
     *
     * The SQLite DB path is taken from:
     * 1) System property "rxnorm.db.path" OR
     * 2) Environment variable "RXNORM_DB_PATH"
     * If both are missing, it uses the default project path:
     *   data/rxnorm/rxnorm.db (relative to project root)
     */
    public static void initializeWithLocalProviders(Neo4jCypherDatabase neo4jDatabase) {
        // Initialize dose form mapper
        DoseFormMapper.initialize(neo4jDatabase);

        String dbPath = System.getProperty("rxnorm.db.path");
        if (dbPath == null || dbPath.isBlank()) {
            String env = System.getenv("RXNORM_DB_PATH");
            if (env != null && !env.isBlank()) {
                dbPath = env;
            } else {
                dbPath = "data/rxnorm/rxnorm.db";
            }
        }

        // Local resolvers/providers
        RxNormProductMatcher.setRxcuiTermTypeResolver(new LocalRxNormTtyResolver(dbPath));
        RxNormProductMatcher.setCandidateProvider(new LocalRxNormCandidateProvider(dbPath));

        sharedMatcher = new RxNormProductMatcher();
    }

    /**
     * Creates a new RxNormProductMatcher instance.
     * 
     * @return A new RxNormProductMatcher instance
     */
    public static RxNormProductMatcher createMatcher() {
        return new RxNormProductMatcher();
    }

    /**
     * Creates a new RxNormProductMatcher instance with API providers already configured.
     * 
     * @param neo4jDatabase The Neo4j database instance for dose form mapping
     * @return A new RxNormProductMatcher instance with providers configured
     */
    public static RxNormProductMatcher createMatcherWithApiProviders(Neo4jCypherDatabase neo4jDatabase) {
        initializeWithApiProviders(neo4jDatabase);
        return new RxNormProductMatcher();
    }

    public static RxNormProductMatcher getSharedMatcher() {
        return sharedMatcher;
    }
}
