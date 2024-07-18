package de.medizininformatikinitiative.medgraph.common.db;

import de.medizininformatikinitiative.medgraph.ApplicationPreferences;
import org.junit.jupiter.api.*;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Markus Budeus
 */
public class ConnectionPreferencesTest {

	private static final String TEST_NODE = "test";
	private static Preferences testNode;
	private ConnectionPreferences sut;

	@BeforeAll
	static void beforeAll() {
		testNode = ApplicationPreferences.getApplicationRootNode().node(TEST_NODE);
	}

	@BeforeEach
	void setUp() {
		sut = new ConnectionPreferences(testNode);
	}

	@Test
	public void defaultConnectionUri() {
		assertEquals(ConnectionPreferences.DEFAULT_URI, sut.getConnectionUri());
	}

	@Test
	public void defaultUsername() {
		assertEquals(ConnectionPreferences.DEFAULT_USER, sut.getUser());
	}

	@Test
	public void defaultPassword() {
		assertNull(sut.getPassword());
	}

	@Test
	public void setConnectionUri() {
		sut.setConnectionUri("neo4j://8.8.8.8:65635");
		assertEquals("neo4j://8.8.8.8:65635", sut.getConnectionUri());
	}

	@Test
	public void setUser() {
		sut.setUser("John Lennon");
		assertEquals("John Lennon", sut.getUser());
	}

	@Test
	public void setPassword() {
		sut.setPassword("myhighlysecurepassword".toCharArray());
		assertArrayEquals("myhighlysecurepassword".toCharArray(), sut.getPassword());
	}

	@Test
	public void clearPassword() {
		sut.setPassword("myhighlysecurepassword".toCharArray());
		sut.clearPassword();
		assertNull(sut.getPassword());
	}

	@AfterEach
	void tearDown() throws BackingStoreException {
		testNode.clear();
	}

	@AfterAll
	static void afterAll() throws BackingStoreException {
		testNode.removeNode();
	}
}