package de.medizininformatikinitiative.medgraph.common.db;

import de.medizininformatikinitiative.medgraph.common.ApplicationPreferences;
import de.medizininformatikinitiative.medgraph.common.logging.Level;
import de.medizininformatikinitiative.medgraph.common.logging.LogManager;
import de.medizininformatikinitiative.medgraph.common.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.neo4j.driver.Session;
import org.neo4j.driver.exceptions.AuthenticationException;
import org.neo4j.driver.exceptions.Neo4jException;

/**
 * Stores connection configuration for a {@link DatabaseConnection}. This class is immutable.
 *
 * @author Markus Budeus
 */
public class ConnectionConfiguration {

	private static final Logger logger = LogManager.getLogger(ConnectionConfiguration.class);

	/**
	 * Default connection configuration.
	 */
	private static ConnectionConfiguration defaultConfig;

	static {
		defaultConfig = new ConnectionConfiguration(ApplicationPreferences.getDatabaseConnectionPreferences());
	}

	/**
	 * Sets the default connection configuration.
	 */
	public static void setDefault(@NotNull ConnectionConfiguration connectionConfiguration) {
		defaultConfig = connectionConfiguration;
		logger.log(Level.DEBUG,
				"Default connection configuration updated: " + connectionConfiguration);
	}

	/**
	 * Returns the configured default connection configuration.
	 */
	public static ConnectionConfiguration getDefault() {
		return defaultConfig;
	}

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
	 *
	 * @param preferences the preferences from which to retrieve the configuration
	 */
	public ConnectionConfiguration(@NotNull ConnectionPreferences preferences) {
		this(preferences.getConnectionUri(), preferences.getUser(), preferences);
	}

	/**
	 * Creates a connection configuration using the given uri and user, but taking the password from the given
	 * preferences.
	 *
	 * @param uri         the connection URI
	 * @param user        the user to authenticate with
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
	public ConnectionConfiguration(@NotNull String uri, @NotNull String user, char @Nullable [] password) {
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
	 *
	 * @return a {@link ConnectionResult} indicating the result of the connection attempt
	 */
	public ConnectionResult testConnection() {
		logger.log(Level.DEBUG, "Running connection test: "+this);
		try (DatabaseConnection connection = createConnection();
		     Session session = connection.createSession()) {
			session.beginTransaction();
			session.close();
			logger.log(Level.DEBUG, "Connection successful!");
			return ConnectionResult.SUCCESS;
		} catch (IllegalArgumentException e) {
			logger.log(Level.INFO, "Invalid connection string! ("+e.getMessage()+")");
			return ConnectionResult.INVALID_CONNECTION_STRING;
		} catch (AuthenticationException e) {
			logger.log(Level.INFO, "Authentication failed! ("+e.getMessage()+")");
			return ConnectionResult.AUTHENTICATION_FAILED;
		} catch (Neo4jException e) {
			logger.log(Level.INFO, "Neo Neo4j service is reachable at " + this.getUri()+". ("+e.getMessage()+")");
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

	@Override
	public String toString() {
		return uri + " (" + user + ")";
	}
}
