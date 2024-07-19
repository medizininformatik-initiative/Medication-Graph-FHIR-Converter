package de.medizininformatikinitiative.medgraph.common.db;

/**
 * Indicates the underlying cause of a failed connection attempt to the Neo4j database.
 *
 * @author Markus Budeus
 */
public enum ConnectionFailureReason {
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