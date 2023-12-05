package de.tum.med.aiim.markusbudeus.graphdbpopulator;

import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Session;

import java.util.function.Consumer;

public class DatabaseConnection implements AutoCloseable {

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
				"neo4j://localhost:7687",
				"neo4j",
				"aiim-experimentation".toCharArray()
		);
	}

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
