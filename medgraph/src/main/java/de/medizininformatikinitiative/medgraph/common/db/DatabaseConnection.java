package de.medizininformatikinitiative.medgraph.common.db;

import de.medizininformatikinitiative.medgraph.common.ApplicationPreferences;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.driver.exceptions.AuthenticationException;
import org.neo4j.driver.exceptions.ServiceUnavailableException;

import java.net.URISyntaxException;
import java.util.function.Consumer;

/**
 * Represents a connection to a Neo4j graph database. Also statically holds authentication information.
 *
 * @author Markus Budeus
 */
public class DatabaseConnection implements AutoCloseable {

	/**
	 * The currently configured uri.
	 */
	@NotNull
	private static String uri = "";
	@NotNull
	private static String user = "";
	private static char @Nullable [] password;

	public enum SaveOption {
		/**
		 * Saves URI, user and password.
		 */
		EVERYTHING,
		/**
		 * Saves URI and user.
		 */
		EXCLUDE_PASSWORD,
		/**
		 * Does not save anything.
		 */
		DONT_SAVE
	}

	static {
		loadData();
	}

	/**
	 * Sets the connection information to be used by default. Keeps the currently configured password.
	 *
	 * @param uri  the connection URI
	 * @param user the user to authenticate with
	 * @param save if true, the given settings are saved
	 */
	public static void setConnection(String uri, String user, boolean save) {
		setConnection(uri, user, DatabaseConnection.password,
				save ? SaveOption.EXCLUDE_PASSWORD : SaveOption.DONT_SAVE);
	}

	/**
	 * Updates the connection information to be used by default.
	 *
	 * @param uri        the connection URI
	 * @param user       the user to authenticate with
	 * @param password   the password to authenticate with
	 * @param saveOption whether and how these settings shall be saved
	 */
	public static void setConnection(String uri, String user, char[] password, SaveOption saveOption) {
		DatabaseConnection.uri = uri;
		DatabaseConnection.user = user;
		DatabaseConnection.password = password;

		switch (saveOption) {
			case EVERYTHING -> {
				saveData(true);
			}
			case EXCLUDE_PASSWORD -> {
				saveData(false);
			}
			case DONT_SAVE -> {

			}
		}
	}

	private static void loadData() {
		ConnectionPreferences prefs = ApplicationPreferences.getDatabaseConnectionPreferences();
		uri = prefs.getConnectionUri();
		user = prefs.getUser();
		password = prefs.getPassword();
	}

	private static void saveData(boolean savePassword) {
		ConnectionPreferences prefs = ApplicationPreferences.getDatabaseConnectionPreferences();
		prefs.setConnectionUri(uri);
		prefs.setUser(user);
		if (savePassword) prefs.setPassword(password);
	}

	/**
	 * Returns the currently configured connection URI.
	 */
	@NotNull
	public static String getUri() {
		return uri;
	}

	/**
	 * Returns the currently configured username.
	 */
	@NotNull
	public static String getUser() {
		return user;
	}

	/**
	 * Returns whether a default password is currently configured.
	 */
	public static boolean hasConfiguredPassword() {
		return password != null;
	}

	private final Driver driver;


	/**
	 * Creates a new connection and a session, which are both automatically closed when the given action exits.
	 */
	public static void runSession(Consumer<Session> action) {
		try (DatabaseConnection connection = new DatabaseConnection(); Session session = connection.createSession()) {
			action.accept(session);
		}
	}

	/**
	 * Creates a new database connection while attempting to reuse the preconfigured connection information.
	 */
	public DatabaseConnection() {
		this(
				uri,
				user
		);
	}

	/**
	 * Creates a new database connection while attempting to reuse the preconfigured password.
	 */
	public DatabaseConnection(String uri, String user) {
		this(
				uri,
				user,
				password != null ? password : new char[0]
		);
	}

	/**
	 * Creates a new database connection.
	 * @param uri the URI to connect to
	 * @param user the username to authenticate with
	 * @param password the password to authenticate with
	 */
	public DatabaseConnection(String uri, String user, char[] password) {
		// Being forced to pass the password as a string is a slight affront to security, but okay.
		driver = GraphDatabase.driver(uri, AuthTokens.basic(user, new String(password)));
	}

	public ConnectionResult testConnection() {
		try {
			Session s = createSession();
			s.beginTransaction();
			s.close();
			return ConnectionResult.SUCCESS;
		} catch (IllegalArgumentException e) {
			return ConnectionResult.INVALID_CONNECTION_STRING;
		} catch (AuthenticationException e) {
			return ConnectionResult.AUTHENTICATION_FAILED;
		} catch (ServiceUnavailableException e) {
			return ConnectionResult.SERVICE_UNAVAILABLE;
		}
	}

	public Session createSession() {
		return driver.session();
	}

	public enum ConnectionResult {
		SUCCESS,
		INVALID_CONNECTION_STRING,
		SERVICE_UNAVAILABLE,
		AUTHENTICATION_FAILED
	}

	@Override
	public void close() {
		driver.close();
	}

}
