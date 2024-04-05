package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import de.medizininformatikinitiative.medgraph.graphdbpopulator.DatabaseConnection;
import org.junit.jupiter.api.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.util.HashSet;
import java.util.Set;

import static de.medizininformatikinitiative.medgraph.graphdbpopulator.DatabaseDefinitions.SYNONYME_LABEL;
import static de.medizininformatikinitiative.medgraph.graphdbpopulator.DatabaseDefinitions.SYNONYME_REFERENCES_NODE_LABEL;
import static org.junit.jupiter.api.Assertions.*;

@Disabled("This test wipes the target database and is therefore disabled by default")
public class DatabaseSynonymePreparerTest {

	private static DatabaseConnection connection;
	private static Session session;

	@BeforeAll
	public static void setUpTest() {
		connection = new DatabaseConnection();
		session = connection.createSession();
	}

	@BeforeEach
	public void clearDb() {
		session.run(
				"MATCH (p) DETACH DELETE p"
		);
	}

	@Test
	public void makeSynonymesLowerCase() {
		session.run(
				"CREATE (s:Test {id: 1}) " +
						"CREATE (t:Test {id: 2}) " +
						"CREATE (sy1:" + SYNONYME_LABEL + " {name: 'thisISs'})-[:" + SYNONYME_REFERENCES_NODE_LABEL + "]->(s) " +
						"CREATE (sy2:" + SYNONYME_LABEL + " {name: 'thisIsT'})-[:" + SYNONYME_REFERENCES_NODE_LABEL + "]->(t) "
		);

		DatabaseSynonymePreparer sut = new DatabaseSynonymePreparer(session);
		sut.makeAllSynonymesLowerCase();

		Result result = session.run(
				"MATCH (sy:" + SYNONYME_LABEL + ")-[:" + SYNONYME_REFERENCES_NODE_LABEL + "]->(t) " +
						"RETURN sy.name, t.id"
		);

		confirmSynonymes(
				Set.of(
						Pair.of("thisiss", 1),
						Pair.of("thisist", 2)
				),
				result);
	}

	@Test
	public void makeSynonymesLowerCase2() {
		session.run(
				"CREATE (s:Test {id: 1}) " +
						"CREATE (t:Test {id: 2}) " +
						"CREATE (u:Test {id: 3}) " +
						"CREATE (:" + SYNONYME_LABEL + " {name: 'thisISs'})-[:" + SYNONYME_REFERENCES_NODE_LABEL + "]->(s) " +
						"CREATE (:" + SYNONYME_LABEL + " {name: 'thisIsT'})-[:" + SYNONYME_REFERENCES_NODE_LABEL + "]->(t) " +
						"CREATE (:" + SYNONYME_LABEL + " {name: 'thisist'})-[:" + SYNONYME_REFERENCES_NODE_LABEL + "]->(t) " +
						"CREATE (:" + SYNONYME_LABEL + " {name: 'thisisu'})-[:" + SYNONYME_REFERENCES_NODE_LABEL + "]->(u) " +
						"CREATE (:" + SYNONYME_LABEL + " {name: 'thisAlsoIsU'})-[:" + SYNONYME_REFERENCES_NODE_LABEL + "]->(u) "
		);

		DatabaseSynonymePreparer sut = new DatabaseSynonymePreparer(session);
		sut.makeAllSynonymesLowerCase();

		Result result = session.run(
				"MATCH (sy:" + SYNONYME_LABEL + ")-[:" + SYNONYME_REFERENCES_NODE_LABEL + "]->(t) " +
						"RETURN sy.name, t.id"
		);

		confirmSynonymes(
				Set.of(
						Pair.of("thisiss", 1),
						Pair.of("thisist", 2), // merge of both previous synonymes!
						Pair.of("thisisu", 3),
						Pair.of("thisalsoisu", 3)
				),
				result);
	}

	private static void confirmSynonymes(Set<Pair<String, Integer>> expectedSynonymes, Result actual) {
		Set<Pair<String, Integer>> expected = new HashSet<>(expectedSynonymes);
		while (!expected.isEmpty()) {
			assertTrue(actual.hasNext());
			Record record = actual.next();

			String synonyme = record.get(0).asString();
			int id = record.get(1).asInt();

			boolean found = false;
			for (Pair<String, Integer> pair : expected) {
				if (pair.getLeft().equals(synonyme) && pair.getRight().equals(id)) {
					found = true;
					expected.remove(pair);
					break;
				}
			}
			if (!found) {
				fail("Found unexpected synonyme in result: (" + synonyme + ": " + id + ")");
			}
		}
		assertFalse(actual.hasNext());
	}

	@AfterAll
	public static void tearDownTest() {
		session.run(
				"MATCH (p) DETACH DELETE p"
		);
		session.close();
		connection.close();
	}

	private static class Pair<K, V> {
		private final K left;
		private final V right;

		private Pair(K left, V right) {
			this.left = left;
			this.right = right;
		}

		static <K, V> Pair<K, V> of(K left, V right) {
			return new Pair<>(left, right);
		}

		public K getLeft() {
			return left;
		}
		public V getRight() {
			return right;
		}
	}

}