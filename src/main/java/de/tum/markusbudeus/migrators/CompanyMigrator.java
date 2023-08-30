package de.tum.markusbudeus.migrators;

import org.neo4j.driver.Query;
import org.neo4j.driver.Session;

import java.io.IOException;
import java.nio.file.Path;

import static de.tum.markusbudeus.DatabaseDefinitions.COMPANY_LABEL;
import static org.neo4j.driver.Values.parameters;


/**
 * Creates company/manufacturer nodes using the company csv.
 */
public class CompanyMigrator extends Migrator {

	private static final int ID_INDEX = 0;
	private static final int NAME_INDEX = 4;
	private static final int SHORT_NAME_INDEX = 3;

	public CompanyMigrator(Path directory, Session session)
	throws IOException {
		super(directory, "COMPANY.CSV", session);
	}

	@Override
	public void migrateLine(String[] line) {
		addNode(
				Integer.parseInt(line[ID_INDEX]),
				line[NAME_INDEX],
				line[SHORT_NAME_INDEX]
		);
	}

	private void addNode(int id, String name, String shortName) {
		session.run(new Query(
				"CREATE (c:" + COMPANY_LABEL + " {name: $name, short_name: $short_name, mmi_id: $mmi_id})",
				parameters("name", name, "short_name", shortName, "mmi_id", id)
		)).consume();
	}

}
