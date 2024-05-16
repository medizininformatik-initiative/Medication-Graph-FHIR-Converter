package de.medizininformatikinitiative.medgraph.common.db;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.neo4j.driver.Session;
import org.neo4j.driver.exceptions.AuthenticationException;
import org.neo4j.driver.exceptions.ServiceUnavailableException;

/**
 * Stores connection configuration for a {@link DatabaseConnection}. This class is immutable.
 *
 * @author Markus Budeus
 */
public class ConnectionConfiguration {

	@NotNull
	private final String uri;
	@NotNull
	private final String user;
	/**
	 * Password may be unconfigured if none was specified by the preferences.
	 */
	private final char @Nullable [] password;

	/**
	 * Creates a connection configuration using data from the given preferences.
	 * @param preferences the preferences from which to retrieve the configuration
	 */
	public ConnectionConfiguration(@NotNull ConnectionPreferences preferences) {
		this(preferences.getConnectionUri(), preferences.getUser(), preferences);
	}

	/**
	 * Creates a connection configuration using the given uri and user, but taking the password from the
	 * given preferences.
	 * @param uri the connection URI
	 * @param user the user to authenticate with
	 * @param preferences the preferences from which to take the password
	 */
	public ConnectionConfiguration(@NotNull String uri, @NotNull String user,
	                               @NotNull ConnectionPreferences preferences) {
		this.uri = uri;
		this.user = user;
		this.password = preferences.getPassword();
	}

	/**
	 * Creates a {@link ConnectionConfiguration} using provided values.
	 *
	 * @param uri      the connection URI to use
	 * @param user     the user to authenticate with
	 * @param password the password to authenticate with
	 */
	public ConnectionConfiguration(@NotNull String uri, @NotNull String user, char @NotNull [] password) {
		this.uri = uri;
		this.user = user;
		this.password = password;
	}

	/**
	 * Returns the configured connection URI.
	 */
	public @NotNull String getUri() {
		return uri;
	}

	/**
	 * Returns the configured user.
	 */
	public @NotNull String getUser() {
		return user;
	}

	/**
	 * Creates a {@link DatabaseConnection}-object using this configuration.
	 */
	public DatabaseConnection createConnection() {
		return new DatabaseConnection(uri, user, password != null ? password : new char[0]);
	}

	/**
	 * Saves this configuration to the given preferences.
	 *
	 * @param preferences  the preferences object to which to save
	 * @param savePassword if true, saves the password as well, otherwise erases the stored password from the
	 *                     preferences if it is present
	 */
	public void save(ConnectionPreferences preferences, boolean savePassword) {
		preferences.setConnectionUri(uri);
		preferences.setUser(user);
		if (savePassword && password != null) {
			preferences.setPassword(password);
		} else {
			preferences.clearPassword();
		}
	}

	/**
	 * Attempts to create a connection to the database using this configuration and reports the result.
	 * @return a {@link ConnectionResult} indicating the result of the connection attempt
	 */
	public ConnectionResult testConnection() {
		try (DatabaseConnection connection = createConnection();
		     Session session = connection.createSession()) {
			session.beginTransaction();
			session.close();
			return ConnectionResult.SUCCESS;
		} catch (IllegalArgumentException e) {
			return ConnectionResult.INVALID_CONNECTION_STRING;
		} catch (AuthenticationException e) {
			return ConnectionResult.AUTHENTICATION_FAILED;
		} catch (ServiceUnavailableException e) {
			return ConnectionResult.SERVICE_UNAVAILABLE;
		}
	}

	/**
	 * Indicates the result of a connection attempt to the Neo4j database.
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
		AUTHENTICATION_FAILED
	}
}
