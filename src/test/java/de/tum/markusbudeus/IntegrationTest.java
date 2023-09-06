package de.tum.markusbudeus;

import org.junit.jupiter.api.Test;
import org.neo4j.driver.Query;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

/**
 * Runs the whole migration on a set of sample files.
 */
public class IntegrationTest {

	// WARNING:
	// This test will completely overwrite the target database

	@Test
	@SuppressWarnings("ConstantConditions")
	public void integrationTest() throws URISyntaxException {
		Path sampleFilesPath = Path.of(IntegrationTest.class.getClassLoader().getResource("sample").toURI());
		DatabaseConnection.runSession(session -> {
			session.run(new Query("MATCH (n) DETACH DELETE n")).consume(); // Delete everything
			try {
				Main.runMigrators(sampleFilesPath, false);
			} catch (IOException | InterruptedException e) {
				throw new RuntimeException("Integration test failed!", e);
			}
		});
	}

}
