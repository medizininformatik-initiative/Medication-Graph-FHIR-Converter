package de.tum.markusbudeus;

import org.neo4j.driver.Query;
import org.neo4j.driver.Session;

import java.io.IOException;

import static de.tum.markusbudeus.Main.CODE_REFERENCE_RELATIONSHIP_NAME;
import static de.tum.markusbudeus.Main.CODING_SYSTEM_LABEL;
import static org.neo4j.driver.Values.parameters;

/**
 * Reads the MMI PharmIndex MOLECULE table and writes the data as new nodes into the Neo4j database.
 */
public class SubstanceMigrator extends Migrator {

	private static final int ID_INDEX = 0;
	private static final int NAME_INDEX = 2;
	private static final int ASK_INDEX = 18;

	protected SubstanceMigrator(CSVReader reader, Session session) {
		super(reader, session);
	}

	@Override
	public void migrate() throws IOException {
		String[] line;
		while ((line = reader.readNext()) != null) {
			int id = Integer.parseInt(line[ID_INDEX]);
			String name = line[NAME_INDEX];
			String ask = line[ASK_INDEX];
			addNode(name, id, ask);
		}
	}

	private void addNode(String name, int id, String ask) {
		session.executeWrite(tx -> {
			var query = new Query(
					"CREATE (s:SUBSTANCE {name: $name, mmi_id: $mmi_id}) " +
							"MERGE (c:ASK:"+CODING_SYSTEM_LABEL+" {code: $ask}) " +
							"CREATE (c)-[r:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]->(s)",
					parameters("name", name, "mmi_id", id, "ask", ask));
			var result = tx.run(query);
			return "";
		});
	}
}
