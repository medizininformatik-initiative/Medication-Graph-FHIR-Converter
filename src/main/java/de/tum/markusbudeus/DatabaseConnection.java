package de.tum.markusbudeus;

import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Session;

public class DatabaseConnection implements AutoCloseable {

	private final Driver driver;

	public DatabaseConnection() {
		this(
				"neo4j+s://c6991061.databases.neo4j.io",
				"neo4j",
				"Qh1OSIk-aiX_x_Bi7MknMZ4raGOiKximLZH4A4YwyN8".toCharArray()
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
