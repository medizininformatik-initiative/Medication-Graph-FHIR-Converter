package de.tum.med.aiim.markusbudeus.graphdbpopulator.migrators;

import de.tum.med.aiim.markusbudeus.graphdbpopulator.CSVReader;
import org.neo4j.driver.Query;
import org.neo4j.driver.Session;

import java.io.IOException;
import java.nio.file.Path;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.*;
import static org.neo4j.driver.Values.parameters;

/**
 * Reads the MMI PharmIndex MOLECULE table and writes the data as new nodes into the Neo4j database.
 * <p>
 * Creates Substance nodes, ASK nodes and CAS nodes.
 */
public class SubstanceMigrator extends Migrator {

	private static final int ID_INDEX = 0;
	private static final int NAME_INDEX = 2;
	private static final int ASK_INDEX = 18;
	private static final int CAS_INDEX = 19;

	public SubstanceMigrator(Path directory, Session session) throws IOException {
		super(directory, "MOLECULE.CSV", session);
	}

	protected SubstanceMigrator(CSVReader reader, Session session) {
		super(reader, session);
	}

	@Override
	public void migrateLine(String[] line) {
		addNode(
				line[NAME_INDEX],
				Integer.parseInt(line[ID_INDEX]),
				line[ASK_INDEX],
				line[CAS_INDEX]
		);
	}

	void addNode(String name, int id, String ask, String cas) {
		session.run(new Query(
				"CREATE (s:" + SUBSTANCE_LABEL + " {name: $name, mmi_id: $mmi_id}) " +
						"MERGE (a:" + ASK_LABEL + ":" + CODING_SYSTEM_LABEL + " {code: $ask}) " +
						"MERGE (c:" + CAS_LABEL + ":" + CODING_SYSTEM_LABEL + " {code: $cas}) " +
						"CREATE (a)-[ra:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]->(s) " +
						"CREATE (c)-[rc:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]->(s)",
				parameters("name", name, "mmi_id", id, "ask", ask, "cas", cas)));
	}
}
