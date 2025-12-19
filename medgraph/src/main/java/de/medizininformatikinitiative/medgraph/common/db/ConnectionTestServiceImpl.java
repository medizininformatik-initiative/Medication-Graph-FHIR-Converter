package de.medizininformatikinitiative.medgraph.common.db;

import org.jetbrains.annotations.NotNull;
import org.neo4j.driver.Session;
import org.neo4j.driver.exceptions.AuthenticationException;
import org.neo4j.driver.exceptions.Neo4jException;

/**
 * @author Markus Budeus
 */
public class ConnectionTestServiceImpl implements ConnectionTestService {

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

}
