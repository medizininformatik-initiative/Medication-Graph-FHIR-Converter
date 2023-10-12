package de.tum.med.aiim.markusbudeus.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.io.IOException;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.*;

/**
 * Reads the MMI PharmIndex MOLECULE table and writes the data as new nodes into the Neo4j database.
 * <p>
 * Creates Substance nodes, ASK nodes and CAS nodes.
 */
public class SubstanceLoader extends Loader {

	private static final String ID = "ID";
	private static final String NAME = "NAME_PLAIN";
	private static final String ASK = "ASKNUMBER";
	private static final String CAS = "CASREGISTRATIONNUMBER";

	public SubstanceLoader(Session session)
	throws IOException {
		super("MOLECULE.CSV", session);
	}

	@Override
	protected void executeLoad() {
		executeQuery(
				"CREATE CONSTRAINT substanceMmiIdConstraint IF NOT EXISTS FOR (s:" + SUBSTANCE_LABEL + ") REQUIRE s.mmiId IS UNIQUE"
		);
		executeQuery(
				"CREATE CONSTRAINT casCodeConstraint IF NOT EXISTS FOR (c:" + CAS_LABEL + ") REQUIRE c.code IS UNIQUE"
		);
		executeQuery(
				"CREATE CONSTRAINT askCodeConstraint IF NOT EXISTS FOR (a:" + ASK_LABEL + ") REQUIRE a.code IS UNIQUE"
		);
		executeQuery(withLoadStatement(
				"CREATE (s:" + SUBSTANCE_LABEL + " {name: " + row(NAME) + ", mmiId: " + intRow(ID) + "}) " +
						"WITH * " +
						"CALL {" +
						"WITH row, s " +
						"WITH * WHERE NOT " + nullIfBlank(row(ASK)) + " IS null " +
						"MERGE (a:" + ASK_LABEL + ":" + CODING_SYSTEM_LABEL + " {code: " + row(ASK) + "}) " +
						"CREATE (a)-[ra:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]->(s) " +
						"}" +
						"CALL {" +
						"WITH row, s " +
						"WITH * WHERE NOT " + nullIfBlank(row(CAS)) + " IS null " +
						"MERGE (c:" + CAS_LABEL + ":" + CODING_SYSTEM_LABEL + " {code: " + row(CAS) + "}) " +
						"CREATE (c)-[rc:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]->(s)" +
						"}"
		));
	}

}
