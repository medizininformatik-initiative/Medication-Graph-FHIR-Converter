package de.medizininformatikinitiative.medgraph;

import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilder;
import org.neo4j.harness.Neo4jBuilders;

import javax.xml.crypto.Data;

/**
 * Test class for tests using a local Neo4j Instance.
 *
 * @author Markus Budeus
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class Neo4jTest extends UnitTest {

	protected Neo4j neo4j;

	@BeforeAll
	void initializeNeo4j() {
		this.neo4j = Neo4jBuilders.newInProcessBuilder()
				.withFixture(getDatabaseFixture())
				.build();
	}

	private String getDatabaseFixture() {
		return """
				TODO
				""";
	}

	@AfterAll
	void closeNeo4j() {
		neo4j.close();
	}


}
