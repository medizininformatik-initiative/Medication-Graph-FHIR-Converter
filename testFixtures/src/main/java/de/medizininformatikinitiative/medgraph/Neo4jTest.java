package de.medizininformatikinitiative.medgraph;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.neo4j.driver.Session;


/**
 * Test class to tests of classes which run queries against the database. Provides a connection and session to a shared
 * local test database with a predefined content. (Defined in {@link Neo4jDatabaseTestExtension}, if you want to see
 * it).
 * <p>
 * Note while the session is not shared between implementations of this class, the connection and the database is!
 * Write access to the test database is therefore highly discouraged, as it may cause other unrelated tests to fail.
 * Extend {@link ReadWriteNeo4jTest} if you want to write to the database.
 *
 * @author Markus Budeus
 */
@ExtendWith(Neo4jDatabaseTestExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class Neo4jTest extends UnitTest {
	protected Session session;

	@BeforeAll
	void initializeNeo4j() {
		session = Neo4jDatabaseTestExtension.getConnection().createSession();
	}

	@AfterAll
	void closeConnection() {
		session.close();
	}

}
