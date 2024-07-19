package de.medizininformatikinitiative.medgraph.common.db;

import org.jetbrains.annotations.NotNull;

/**
 * Indicates that an attempt to start a {@link DatabaseConnection} was not successful.
 *
 * @author Markus Budeus
 */
public class DatabaseConnectionException extends Exception {

	/**
	 * The result of the connection attempt (i.e. the reason why the database connection was unsuccessful.)
	 */
	@NotNull
	private final ConnectionFailureReason connectionFailureReason;

	public DatabaseConnectionException(@NotNull ConnectionFailureReason connectionFailureReason, String message) {
		super(message);
		this.connectionFailureReason = connectionFailureReason;
	}

	public DatabaseConnectionException(@NotNull ConnectionFailureReason connectionFailureReason, String message, Throwable cause) {
		super(message, cause);
		this.connectionFailureReason = connectionFailureReason;
	}

	@NotNull
	public ConnectionFailureReason getConnectionResult() {
		return connectionFailureReason;
	}
}
