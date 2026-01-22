package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.searchengine.db.Neo4jCypherDatabase;

/**
 * Utility class to set up RxNormProductMatcher with different provider implementations.
 * <p>
 * Provides methods to initialize the matcher with either:
 * <ul>
 *   <li>Local SQLite-backed providers ({@link LocalRxNormCandidateProvider}, {@link LocalRxNormTtyResolver}) - currently used</li>
 *   <li>API-based providers ({@link RxNavCandidateProvider}, {@link RxNavTtyResolver}) - not currently used</li>
 * </ul>
 *
 * @author Lucy Strüfing
 */
public final class RxNormMatcherSetup {

    private RxNormMatcherSetup() {}

    private static volatile RxNormProductMatcher sharedMatcher;

    /**
     * Initializes RxNormProductMatcher with API-based providers.
     * <p>
     * Sets up RxNav API-based implementations for candidate generation and TTY resolution.
     * <b>Note:</b> This method is currently not used - the system uses local SQLite providers instead.
     *
     * @param neo4jDatabase the Neo4j database instance for dose form mapping
     */
    public static void initializeWithApiProviders(Neo4jCypherDatabase neo4jDatabase) {
        DoseFormMapper.initialize(neo4jDatabase);
        RxNormProductMatcher.setRxcuiTermTypeResolver(new RxNavTtyResolver());
        RxNormProductMatcher.setCandidateProvider(new RxNavCandidateProvider());
        sharedMatcher = new RxNormProductMatcher();
    }

    /**
     * Initializes RxNormProductMatcher with local SQLite-backed providers.
     * <p>
     * This is the currently used initialization method. Sets up local SQLite-based implementations
     * for candidate generation and TTY resolution.
     * <p>
     * The SQLite database path is determined in the following order:
     * <ol>
     *   <li>System property "rxnorm.db.path"</li>
     *   <li>Environment variable "RXNORM_DB_PATH"</li>
     *   <li>Default: "data/rxnorm/rxnorm.db" (relative to project root)</li>
     * </ol>
     *
     * @param neo4jDatabase the Neo4j database instance for dose form mapping
     */
    public static void initializeWithLocalProviders(Neo4jCypherDatabase neo4jDatabase) {
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

        RxNormProductMatcher.setRxcuiTermTypeResolver(new LocalRxNormTtyResolver(dbPath));
        RxNormProductMatcher.setCandidateProvider(new LocalRxNormCandidateProvider(dbPath));
        sharedMatcher = new RxNormProductMatcher();
    }

    /**
     * Creates a new RxNormProductMatcher instance.
     * <p>
     * The matcher will use the providers configured via {@link #initializeWithLocalProviders(Neo4jCypherDatabase)}
     * or {@link #initializeWithApiProviders(Neo4jCypherDatabase)}.
     *
     * @return a new RxNormProductMatcher instance
     */
    public static RxNormProductMatcher createMatcher() {
        return new RxNormProductMatcher();
    }

    /**
     * Creates a new RxNormProductMatcher instance with API providers already configured.
     * <p>
     * <b>Note:</b> This method is currently not used - the system uses local SQLite providers instead.
     *
     * @param neo4jDatabase the Neo4j database instance for dose form mapping
     * @return a new RxNormProductMatcher instance with API providers configured
     */
    public static RxNormProductMatcher createMatcherWithApiProviders(Neo4jCypherDatabase neo4jDatabase) {
        initializeWithApiProviders(neo4jDatabase);
        return new RxNormProductMatcher();
    }

    /**
     * Returns the shared RxNormProductMatcher instance.
     * <p>
     * The shared matcher is created when {@link #initializeWithLocalProviders(Neo4jCypherDatabase)}
     * or {@link #initializeWithApiProviders(Neo4jCypherDatabase)} is called.
     *
     * @return the shared matcher instance, or null if not yet initialized
     */
    public static RxNormProductMatcher getSharedMatcher() {
        return sharedMatcher;
    }
}
