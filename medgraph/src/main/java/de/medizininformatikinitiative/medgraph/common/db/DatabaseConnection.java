package de.medizininformatikinitiative.medgraph.common.db;

import de.medizininformatikinitiative.medgraph.common.ApplicationPreferences;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.driver.exceptions.AuthenticationException;
import org.neo4j.driver.exceptions.ServiceUnavailableException;

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
	static String uri;
	static String user;
	private static char[] password;

	static {
		loadData();
	}

	public static void setConnection(String uri, String user, char[] password, boolean savePassword) {
		DatabaseConnection.uri = uri;
		DatabaseConnection.user = user;
		DatabaseConnection.password = password;
		saveData(savePassword);
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

	private final Driver driver;


	/**
	 * Creates a new connection and a session, which are both automatically closed when the given action exits.
	 */
	public static void runSession(Consumer<Session> action) {
		try (DatabaseConnection connection = new DatabaseConnection(); Session session = connection.createSession()) {
			action.accept(session);
		}
	}

	public DatabaseConnection() {
		this(
				uri,
				user,
				password
		);
	}

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
