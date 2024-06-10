package de.medizininformatikinitiative.medgraph;

import de.medizininformatikinitiative.medgraph.common.db.DatabaseTools;
import org.junit.jupiter.api.AfterAll;

/**
 * Extension of {@link Neo4jTest} which resets the database after all tests have run, allowing the test to make
 * write operations.
 *
 * @author Markus Budeus
 */
public class ReadWriteNeo4jTest extends Neo4jTest {

	@AfterAll
	void resetDatabase() {
		DatabaseTools.clearDatabase(session);
		session.run(Neo4jDatabaseTestExtension.getDatabaseFixture());
	}
}
