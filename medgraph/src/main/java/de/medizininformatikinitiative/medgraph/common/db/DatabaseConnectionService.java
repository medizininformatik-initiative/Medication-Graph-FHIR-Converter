package de.medizininformatikinitiative.medgraph.common.db;

import org.jetbrains.annotations.NotNull;

/**
 * Service which provides database connections and utility to test connection configurations.
 *
 * @author Markus Budeus
 */
public interface DatabaseConnectionService {

	/**
	 * Creates and returns a new database connection if possible. Runs a short test using the given connection before
	 * returning it.
	 *
	 * @throws DatabaseConnectionException if the database connection could not be established or the connection test
	 *                                     failed
	 */
	@NotNull
	default DatabaseConnection createConnection() throws DatabaseConnectionException {
		return createConnection(true);
	}

	/**
	 * Creates and returns a new database connection if possible using the current default configuration.
	 *
	 * @param test if true, runs a short test with the database connection before returning it
	 * @throws DatabaseConnectionException if the database connection could not be established or testing is enabled and
	 *                                     the connection test failed
	 */
	@NotNull
	DatabaseConnection createConnection(boolean test) throws DatabaseConnectionException;

	/**
	 * Tests the current default configuration to ensure a successful database connection can be established.
	 * If it can, this method simply returns.
	 * @throws DatabaseConnectionException if the connection could not be established
	 */
	default void verifyConnection() throws DatabaseConnectionException {
		createConnection(true).close();
	}

}
