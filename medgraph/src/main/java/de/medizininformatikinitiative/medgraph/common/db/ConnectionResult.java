package de.medizininformatikinitiative.medgraph.common.db;

/**
 * Indicates the result of a connection attempt to the Neo4j database.
 *
 * @author Markus Budeus
 */
public enum ConnectionResult {
	/**
	 * The connection was successful.
	 */
	SUCCESS,
	/**
	 * The provided connection URI is invalid.
	 */
	INVALID_CONNECTION_STRING,
	/**
	 * No Neo4j service was reachable using the given connection URI.
	 */
	SERVICE_UNAVAILABLE,
	/**
	 * The authentication was unsuccessful.
	 */
	AUTHENTICATION_FAILED,
	/**
	 * An unexpected error occurred.
	 */
	INTERNAL_ERROR,
}