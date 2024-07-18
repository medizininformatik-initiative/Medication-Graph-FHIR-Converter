package de.medizininformatikinitiative.medgraph.common.db;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Service which provides database connections.
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
	 * Creates and returns a new database connection if possible.
	 *
	 * @param test if true, runs a short test with the database connection before returning it
	 * @throws DatabaseConnectionException if the database connection could not be established or testing is enabled and
	 *                                     the connection test failed
	 */
	@NotNull
	DatabaseConnection createConnection(boolean test) throws DatabaseConnectionException;

}
