package de.medizininformatikinitiative.medgraph.common.db;

import de.medizininformatikinitiative.medgraph.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

/**
 * @author Markus Budeus
 */
@SuppressWarnings("resource")
public class ApplicationDatabaseConnectionManagerTest extends UnitTest {

	private static final String URI = "neo4j://fakeuri";
	private static final String USER = "neo5j";
	private static final char[] PASSWORD = "unsafe!".toCharArray();

	@Mock
	private ConnectionTestService connectionTestService;
	@Mock
	private DatabaseConnection connection;
	@Mock
	private ConnectionPreferences preferences;

	private ApplicationDatabaseConnectionManager sut;

	@BeforeEach
	void setUp() throws DatabaseConnectionException {
		when(preferences.getConnectionUri()).thenReturn(URI);
		when(preferences.getUser()).thenReturn(USER);
		when(preferences.getPassword()).thenReturn(PASSWORD);
		insertMockDependency(ConnectionTestService.class, connectionTestService);
		when(connectionTestService.createConnection(any(), anyBoolean())).thenReturn(connection);
	}

	@Test
	void setUpFromPrefs() throws DatabaseConnectionException {
		sut = new ApplicationDatabaseConnectionManager(preferences);

		DatabaseConnection con = sut.createConnection();
		assertSame(connection, con);
		ConnectionConfiguration expectedConfig = new ConnectionConfiguration(URI, USER, PASSWORD);
		assertEquals(expectedConfig, sut.getConnectionConfiguration());
		verify(connectionTestService).createConnection(eq(expectedConfig), anyBoolean());
	}

	@Test
	void setUpWithSpecificConfig() throws DatabaseConnectionException {
		ConnectionConfiguration config = mock();
		sut = new ApplicationDatabaseConnectionManager(preferences, config);

		assertSame(config, sut.getConnectionConfiguration());
		DatabaseConnection con = sut.createConnection();
		assertSame(connection, con);

		verify(connectionTestService).createConnection(eq(config), anyBoolean());
	}

	@Test
	void overwriteConfig() throws DatabaseConnectionException {
		sut = new ApplicationDatabaseConnectionManager(preferences);
		ConnectionConfiguration config = mock();

		sut.setConnectionConfiguration(config, ConnectionConfigurationService.SaveOption.DONT_SAVE);

		assertSame(config, sut.getConnectionConfiguration());
		DatabaseConnection con = sut.createConnection();
		assertSame(connection, con);
	}

	@ParameterizedTest
	@EnumSource
	void saveOptions(ConnectionConfigurationService.SaveOption saveOption) {
		sut = new ApplicationDatabaseConnectionManager(preferences);
		ConnectionConfiguration config = new ConnectionConfiguration(
				"neo4j://uri",
				"jim",
				"mysecurepassword".toCharArray()
		);

		sut.setConnectionConfiguration(config, saveOption);

		if (saveOption == ConnectionConfigurationService.SaveOption.DONT_SAVE) {
			verify(preferences, never()).setUser(any());
			verify(preferences, never()).setConnectionUri(any());
			verify(preferences, never()).setPassword(any());
		} else {
			verify(preferences).setConnectionUri("neo4j://uri");
			verify(preferences).setUser("jim");
			if (saveOption == ConnectionConfigurationService.SaveOption.EXCLUDE_PASSWORD) {
				verify(preferences).clearPassword();
			} else {
				verify(preferences).setPassword("mysecurepassword".toCharArray());
			}
		}
	}

}