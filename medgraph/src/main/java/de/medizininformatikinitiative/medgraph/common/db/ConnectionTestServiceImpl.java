package de.medizininformatikinitiative.medgraph.common.db;

import de.medizininformatikinitiative.medgraph.common.logging.Level;
import de.medizininformatikinitiative.medgraph.common.logging.LogManager;
import de.medizininformatikinitiative.medgraph.common.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.neo4j.driver.Session;
import org.neo4j.driver.exceptions.AuthenticationException;
import org.neo4j.driver.exceptions.Neo4jException;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Markus Budeus
 */
public class ConnectionTestServiceImpl implements ConnectionTestService {

    private final static Logger logger = LogManager.getLogger("ConnectionTest");


    private static final long BYTE = 1L;
    private static final long KiB = BYTE << 10;
    private static final long MiB = KiB << 10;
    private static final long GiB = MiB << 10;

    private static final String HEAP_INITIAL_SIZE_SETTING = "server.memory.heap.initial_size";
    private static final long RECOMMENDED_HEAP_INITIAL_SIZE_MEBIBYTES = 2048L;
    private static final String HEAP_MAX_SIZE_SETTING = "server.memory.heap.max_size";
    private static final long RECOMMENDED_HEAP_MAX_SIZE_MEBIBYTES = 3072;
    private static final String TRANSACTION_SIZE_SETTING = "dbms.memory.transaction.total.max";
    private static final long RECOMMENDED_TRANSACTION_SIZE_MEBIBYTES = 2048L;

    /**
     * Creates a new {@link DatabaseConnection} using the given {@link ConnectionConfiguration} and optionally tests
     * it.
     *
     * @param connectionConfiguration the configuration which to use for the connection
     * @param test                    if true, makes a simple test to ensure the connection is functional
     * @return the {@link DatabaseConnection}
     * @throws DatabaseConnectionException if creating the connection failed or the test is enabled and failed
     */
    @NotNull
    public DatabaseConnection createConnection(ConnectionConfiguration connectionConfiguration, boolean test)
            throws DatabaseConnectionException {
        DatabaseConnection connection;
        try {
            connection = connectionConfiguration.createConnection();
        } catch (IllegalArgumentException e) {
            throw new DatabaseConnectionException(ConnectionFailureReason.INVALID_CONNECTION_STRING,
                    "Invalid connection string! (" + e.getMessage() + ")");
        }

        if (!test) return connection;

        try (Session session = connection.createSession()) {
            verifyNeo4jSizeLimits(session);
            session.beginTransaction();
            return connection;
        } catch (Exception e) {
            connection.close();
            if (e instanceof AuthenticationException) {
                throw new DatabaseConnectionException(ConnectionFailureReason.AUTHENTICATION_FAILED,
                        "Authentication failed! (" + e.getMessage() + ")");
            } else if (e instanceof Neo4jException) {
                throw new DatabaseConnectionException(ConnectionFailureReason.SERVICE_UNAVAILABLE,
                        "Neo Neo4j service is reachable at " + connectionConfiguration.getUri() + ". (" + e.getMessage() + ")");
            } else {
                throw new DatabaseConnectionException(ConnectionFailureReason.INTERNAL_ERROR,
                        "Something went wrong while trying to connect to the Neo4j database.", e);
            }
        }
    }

    private void verifyNeo4jSizeLimits(Session session) {
        Map<String, String> relevantSettings = session.run("SHOW SETTINGS;")
                .stream()
                .filter(r -> List.of(HEAP_INITIAL_SIZE_SETTING, HEAP_MAX_SIZE_SETTING, TRANSACTION_SIZE_SETTING).contains(r.get("name").asString()))
                .collect(Collectors.toMap((r) -> r.get("name").asString(), (r) -> r.get("value").asString()));
        Pattern valuePattern = Pattern.compile("^(?<val>[0-9.]+)(?<unit>mib|mb|m|gib|gb|g)$");

        Map<String, Long> limitMap = Map.of(
                HEAP_INITIAL_SIZE_SETTING, RECOMMENDED_HEAP_INITIAL_SIZE_MEBIBYTES,
                HEAP_MAX_SIZE_SETTING, RECOMMENDED_HEAP_MAX_SIZE_MEBIBYTES,
                TRANSACTION_SIZE_SETTING, RECOMMENDED_TRANSACTION_SIZE_MEBIBYTES
        );

        Map.of(
                HEAP_INITIAL_SIZE_SETTING, "Heap initial size setting",
                HEAP_MAX_SIZE_SETTING, "Heap maximum size setting",
                TRANSACTION_SIZE_SETTING, "Transaction size limit setting (dynamically set to 70% of the available heap space)").forEach((setting, description) -> {
            String stringValue = relevantSettings.get(setting).toLowerCase();
            var matcher = valuePattern.matcher(stringValue);
            long recommendedLimit = limitMap.get(setting);
            if (!matcher.matches()) {
                logger.log(Level.WARN, "Could not parse %s (Neo4J setting '%s'): '%s'. It might be set either too low (recommended value: %d MiB), or very large, which would be fine.".formatted(description, setting, stringValue, recommendedLimit));
                return;
            }
            long bytes = getBytes(matcher);
            long mebibytes = bytes / MiB;
            if (mebibytes < recommendedLimit) {
                String message = "Your %s is set to %d MiB, which is below the recommended limit of %d MiB. Please consider increasing it to at least %d MiB using the neo4j setting '%s'.".formatted(description, mebibytes, recommendedLimit, recommendedLimit, setting);
                logger.log(Level.WARN, message);
            }
        });
    }

    /**
     * Convert a Neo4J size setting to bytes. The provided matcher must have two groups, 'val' and 'unit'.
     * Only lower-case values can be parsed.
     * Implementation inspired by https://www.baeldung.com/java-human-readable-byte-size
     *
     * @param matcher the input matcher
     * @return the number of bytes
     */
    private static long getBytes(Matcher matcher) {
        double numericValue = Double.parseDouble(matcher.group("val"));
        String unit = matcher.group("unit");
        // From the Neo4J settings file at version 5.26.7:
        // Memory settings are specified kibibytes with the 'k' suffix, mebibytes with 'm' and gibibytes with 'g'.

        // However, the online documentation allows more values: B, KiB, KB, K, kB, kb, k, MiB, MB, M, mB, mb, m, GiB, GB, G, gB, gb, g, TiB, TB, PiB, PB, EiB, EB
        // The switch statement is reduced to 'reasonable' units.
        double bytesLong = switch (unit) {
            case "mib", "m", "mb" -> numericValue * MiB;
            case "gib", "g", "gb" -> numericValue * GiB;
            default -> throw new IllegalStateException("Unexpected value: " + unit);
        };
        return (long) bytesLong;
    }

}
