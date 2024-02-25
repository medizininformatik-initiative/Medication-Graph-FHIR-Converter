package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import de.medizininformatikinitiative.medgraph.graphdbpopulator.DatabaseDefinitions;
import org.neo4j.driver.Session;

import java.io.IOException;

/**
 * Reads the MMI PharmIndex MOLECULE table and writes the data as new nodes into the Neo4j database.
 * <p>
 * Creates Substance nodes, ASK nodes and CAS nodes.
 *
 * @author Markus Budeus
 */
public class SubstanceLoader extends CsvLoader {

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
				"CREATE CONSTRAINT substanceMmiIdConstraint IF NOT EXISTS FOR (s:" + DatabaseDefinitions.SUBSTANCE_LABEL + ") REQUIRE s.mmiId IS UNIQUE"
		);
		executeQuery(
				"CREATE CONSTRAINT casCodeConstraint IF NOT EXISTS FOR (c:" + DatabaseDefinitions.CAS_LABEL + ") REQUIRE c.code IS UNIQUE"
		);
		executeQuery(
				"CREATE CONSTRAINT askCodeConstraint IF NOT EXISTS FOR (a:" + DatabaseDefinitions.ASK_LABEL + ") REQUIRE a.code IS UNIQUE"
		);
		executeQuery(withLoadStatement(
				"CREATE (s:" + DatabaseDefinitions.SUBSTANCE_LABEL + " {name: " + row(NAME) + ", mmiId: " + intRow(ID) + "}) " +
						"WITH * " +
						"CALL {" +
						"WITH row, s " +
						"WITH * WHERE NOT " + nullIfBlank(row(ASK)) + " IS null " +
						"MERGE (a:" + DatabaseDefinitions.ASK_LABEL + ":" + DatabaseDefinitions.CODE_LABEL + " {code: " + row(ASK) + "}) " +
						"CREATE (a)-[ra:" + DatabaseDefinitions.CODE_REFERENCE_RELATIONSHIP_NAME + "]->(s) " +
						"}" +
						"CALL {" +
						"WITH row, s " +
						"WITH * WHERE NOT " + nullIfBlank(row(CAS)) + " IS null " +
						"MERGE (c:" + DatabaseDefinitions.CAS_LABEL + ":" + DatabaseDefinitions.CODE_LABEL + " {code: " + row(CAS) + "}) " +
						"CREATE (c)-[rc:" + DatabaseDefinitions.CODE_REFERENCE_RELATIONSHIP_NAME + " {primary: true}]->(s)" +
						"}"
		));
	}

}
