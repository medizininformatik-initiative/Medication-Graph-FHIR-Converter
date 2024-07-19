package de.medizininformatikinitiative.medgraph.common.db;

import de.medizininformatikinitiative.medgraph.DI;
import de.medizininformatikinitiative.medgraph.Neo4jTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Markus Budeus
 */
public class ConnectionTestServiceImplTest extends Neo4jTest {

	private ConnectionTestServiceImpl sut;

	@BeforeEach
	void setUp() {
		sut = new ConnectionTestServiceImpl();
	}


	@Test
	void connectionWorks() throws DatabaseConnectionException {
		ConnectionConfiguration configuration = DI.get(ConnectionConfigurationService.class)
		                                          .getConnectionConfiguration();
		sut.createConnection(configuration, true).close(); // Should run without throwing
	}

	@Test
	void invalidUri() {
		DatabaseConnectionException exception = assertThrows(DatabaseConnectionException.class, () -> {
			sut.createConnection(
					new ConnectionConfiguration("wha/\\is::this?", "yeah", new char[0]),
					true
			).close();
		});

		assertEquals(ConnectionFailureReason.INVALID_CONNECTION_STRING, exception.getFailureReason());
	}

	@Test
	void neo4jUnreachable() {
		DatabaseConnectionException exception = assertThrows(DatabaseConnectionException.class, () -> {
			sut.createConnection(
					new ConnectionConfiguration("neo4j://localhost:65535", "no way", new char[0]),
					true
			).close();
		});
		assertEquals(ConnectionFailureReason.SERVICE_UNAVAILABLE, exception.getFailureReason());
	}

	@Test
	void neo4jUnreachableButNoTest() throws DatabaseConnectionException {
		sut.createConnection(
				new ConnectionConfiguration("neo4j://localhost:65535", "no way", new char[0]),
				false
		).close(); // Should run without issues
	}

	// Sadly I haven't found a way to test authentication. I can enable authentication on the test harness, but then
	// it expects me to change the default password, which I don't know how to do programatically.

	@Test
	void authenticationFailedButNoTest() throws DatabaseConnectionException {
		ConnectionConfiguration configuration = DI.get(ConnectionConfigurationService.class)
		                                          .getConnectionConfiguration();
		ConnectionConfiguration invalidConfig = new ConnectionConfiguration(configuration.getUri(),
				"invalidUser", "invalidPass".toCharArray());

		sut.createConnection(invalidConfig, false).close(); // Should run without issues
	}

}