package de.tum.med.aiim.markusbudeus.matcher;

import de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseConnection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.neo4j.driver.Session;

public class TestWithSession {

	protected static DatabaseConnection connection;
	protected static Session session;

	@BeforeAll
	public static void setupSession() {
		connection = new DatabaseConnection();
		session = connection.createSession();
	}

	@AfterAll
	public static void tearDownSession() {
		session.close();
		connection.close();
	}


}
