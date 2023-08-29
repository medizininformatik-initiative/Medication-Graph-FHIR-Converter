package de.tum.markusbudeus.migrators;

import org.neo4j.driver.Query;
import org.neo4j.driver.Session;

import java.io.IOException;
import java.nio.file.Path;

import static de.tum.markusbudeus.DatabaseDefinitions.DRUG_LABEL;
import static org.neo4j.driver.Values.parameters;

/**
 * This class creates the Drug nodes in the database using the PRODUCT table from the MMI PharmIndex.
 */
public class DrugMigrator extends Migrator {

	private static final int ID_INDEX = 0;
	private static final int NAME_INDEX = 2;

	public DrugMigrator(Path directory, Session session) throws IOException {
		super(directory, "PRODUCT.csv", session);
	}

	@Override
	public void migrateLine(String[] line) {
		addNode(Integer.parseInt(line[ID_INDEX]), line[NAME_INDEX]);
	}

	void addNode(int id, String name) {
		session.run(new Query(
				"CREATE (d:" + DRUG_LABEL + " {name: $name, mmi_id: $mmi_id})",
				parameters("name", name, "mmi_id", id)
		)).consume();
	}

}
