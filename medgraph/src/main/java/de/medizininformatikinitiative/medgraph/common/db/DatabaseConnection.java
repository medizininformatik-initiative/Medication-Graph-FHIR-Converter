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
 * Represents a connection to a Neo4j graph database.
 *
 * @author Markus Budeus
 */
public class DatabaseConnection implements AutoCloseable {

	/**
	 * Default connection configuration.
	 */
	private static ConnectionConfiguration DEFAULT_CONFIG;

	static {
		DEFAULT_CONFIG = new ConnectionConfiguration(ApplicationPreferences.getDatabaseConnectionPreferences());
	}

	public static void setDefaultConfiguration(@NotNull ConnectionConfiguration connectionConfiguration) {
		DEFAULT_CONFIG = connectionConfiguration;
	}

	/**
	 * Attempts to create a database connection using the current default connection configuration.
	 */
	public static DatabaseConnection createDefault() {
		return DEFAULT_CONFIG.createConnection();
	}

	private final Driver driver;

	/**
	 * Creates a new connection and a session, which are both automatically closed when the given action exits.
	 */
	public static void runSession(Consumer<Session> action) {
		try (DatabaseConnection connection = createDefault(); Session session = connection.createSession()) {
			action.accept(session);
		}
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

	public Session createSession() {
		return driver.session();
	}

	@Override
	public void close() {
		driver.close();
	}

}
