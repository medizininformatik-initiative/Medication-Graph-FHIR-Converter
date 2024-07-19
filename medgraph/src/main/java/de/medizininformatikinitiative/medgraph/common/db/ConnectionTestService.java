package de.medizininformatikinitiative.medgraph.common.db;

import org.jetbrains.annotations.NotNull;

/**
 * Service which can instantiate and test a database connection from a connection config.
 *
 * @author Markus Budeus
 */
public interface ConnectionTestService {

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
	DatabaseConnection createConnection(ConnectionConfiguration connectionConfiguration, boolean test) throws DatabaseConnectionException;

	/**
	 * Tests the given connection configuration and verifies a successful database connection can be established.
	 * If it can, this method simply returns.
	 * @param connectionConfiguration the connection configuration to test
	 * @throws DatabaseConnectionException if the connection could not be established
	 */
	default void verifyConnection(ConnectionConfiguration connectionConfiguration) throws DatabaseConnectionException {
		createConnection(connectionConfiguration, true).close();
	}

}
