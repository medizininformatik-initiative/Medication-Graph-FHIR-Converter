package de.medizininformatikinitiative.medgraph.common.db;

/**
 * Indicates that an attempt to start a {@link DatabaseConnection} was not successful.
 *
 * @author Markus Budeus
 */
public class DatabaseConnectionException extends Exception {

	/**
	 * The result of the connection attempt (i.e. the reason why the database connection was unsuccessful.)
	 */
	private final ConnectionResult connectionResult;

	public DatabaseConnectionException(ConnectionResult connectionResult, String message) {
		super(message);
		this.connectionResult = connectionResult;
	}

	public DatabaseConnectionException(ConnectionResult connectionResult, String message, Throwable cause) {
		super(message, cause);
		this.connectionResult = connectionResult;
	}

	public ConnectionResult getConnectionResult() {
		return connectionResult;
	}
}
