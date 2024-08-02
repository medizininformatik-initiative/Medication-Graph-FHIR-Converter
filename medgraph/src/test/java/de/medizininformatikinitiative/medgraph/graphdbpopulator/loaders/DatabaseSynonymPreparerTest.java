package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import de.medizininformatikinitiative.medgraph.ReadWriteNeo4jTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;

import java.util.HashSet;
import java.util.Set;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.SYNONYM_LABEL;
import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.SYNONYM_REFERENCES_NODE_LABEL;
import static org.junit.jupiter.api.Assertions.*;

public class DatabaseSynonymPreparerTest extends ReadWriteNeo4jTest {

	@BeforeEach
	public void clearDb() {
		session.run(
				"MATCH (p) DETACH DELETE p"
		);
	}

	@Test
	public void makeSynonymsLowerCase() {
		session.run(
				"CREATE (s:Test {id: 1}) " +
						"CREATE (t:Test {id: 2}) " +
						"CREATE (sy1:" + SYNONYM_LABEL + " {name: 'thisISs'})-[:" + SYNONYM_REFERENCES_NODE_LABEL + "]->(s) " +
						"CREATE (sy2:" + SYNONYM_LABEL + " {name: 'thisIsT'})-[:" + SYNONYM_REFERENCES_NODE_LABEL + "]->(t) "
		);

		DatabaseSynonymPreparer sut = new DatabaseSynonymPreparer(session);
		sut.makeAllSynonymsLowerCase();

		Result result = session.run(
				"MATCH (sy:" + SYNONYM_LABEL + ")-[:" + SYNONYM_REFERENCES_NODE_LABEL + "]->(t) " +
						"RETURN sy.name, t.id"
		);

		confirmSynonyms(
				Set.of(
						Pair.of("thisiss", 1),
						Pair.of("thisist", 2)
				),
				result);
	}

	@Test
	public void makeSynonymsLowerCase2() {
		session.run(
				"CREATE (s:Test {id: 1}) " +
						"CREATE (t:Test {id: 2}) " +
						"CREATE (u:Test {id: 3}) " +
						"CREATE (:" + SYNONYM_LABEL + " {name: 'thisISs'})-[:" + SYNONYM_REFERENCES_NODE_LABEL + "]->(s) " +
						"CREATE (:" + SYNONYM_LABEL + " {name: 'thisIsT'})-[:" + SYNONYM_REFERENCES_NODE_LABEL + "]->(t) " +
						"CREATE (:" + SYNONYM_LABEL + " {name: 'thisist'})-[:" + SYNONYM_REFERENCES_NODE_LABEL + "]->(t) " +
						"CREATE (:" + SYNONYM_LABEL + " {name: 'thisisu'})-[:" + SYNONYM_REFERENCES_NODE_LABEL + "]->(u) " +
						"CREATE (:" + SYNONYM_LABEL + " {name: 'thisAlsoIsU'})-[:" + SYNONYM_REFERENCES_NODE_LABEL + "]->(u) "
		);

		DatabaseSynonymPreparer sut = new DatabaseSynonymPreparer(session);
		sut.makeAllSynonymsLowerCase();

		Result result = session.run(
				"MATCH (sy:" + SYNONYM_LABEL + ")-[:" + SYNONYM_REFERENCES_NODE_LABEL + "]->(t) " +
						"RETURN sy.name, t.id"
		);

		confirmSynonyms(
				Set.of(
						Pair.of("thisiss", 1),
						Pair.of("thisist", 2), // merge of both previous synonyms!
						Pair.of("thisisu", 3),
						Pair.of("thisalsoisu", 3)
				),
				result);
	}

	private static void confirmSynonyms(Set<Pair<String, Integer>> expectedSynonyms, Result actual) {
		Set<Pair<String, Integer>> expected = new HashSet<>(expectedSynonyms);
		while (!expected.isEmpty()) {
			assertTrue(actual.hasNext());
			Record record = actual.next();

			String synonym = record.get(0).asString();
			int id = record.get(1).asInt();

			boolean found = false;
			for (Pair<String, Integer> pair : expected) {
				if (pair.getLeft().equals(synonym) && pair.getRight().equals(id)) {
					found = true;
					expected.remove(pair);
					break;
				}
			}
			if (!found) {
				fail("Found unexpected synonym in result: (" + synonym + ": " + id + ")");
			}
		}
		assertFalse(actual.hasNext());
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