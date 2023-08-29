package de.tum.markusbudeus.migrators;

import de.tum.markusbudeus.CSVReader;
import de.tum.markusbudeus.DatabaseConnection;
import org.neo4j.driver.Query;
import org.neo4j.driver.Session;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

import static de.tum.markusbudeus.DatabaseDefinitions.*;
import static org.neo4j.driver.Values.parameters;

/**
 * Creates INN nodes and references to ASK nodes. ASK Nodes are created as necessary, but will be resolved if they
 * already exist.
 */
public class InnMigrator extends Migrator {

	private static final int NAME_INDEX = 0;
	private static final int CAS_INDEX = 7;
	private static final String cas_regex = "[0-9\\-]+";

	/**
	 * Number of entries with cas given as "no mention"
	 */
	private int unmentioned_cas = 0;


	// Data is taken from
	// https://www.wcoomd.org/en/topics/nomenclature/instrument-and-tools/tools-to-assist-with-the-classification-in-the-hs/hs_classification-decisions/inn-table.aspx

	public static void main(String[] args) {
		DatabaseConnection.runSession(session -> {
			try {
				new InnMigrator(session).migrate();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	private static Path resolveFilePath() {
		URL url = InnMigrator.class.getClassLoader().getResource("inn_list.csv");
		Path path;
		try {
			assert url != null;
			path = Path.of(url.toURI());
		} catch (URISyntaxException e) {
			path = Path.of(url.getPath());
		}
		return path;
	}

	protected InnMigrator(Session session) throws IOException {
		super(CSVReader.open(resolveFilePath()), session);
	}

	@Override
	public void migrate() throws IOException {
		unmentioned_cas = 0;
		super.migrate();
		System.out.println("INN Migration complete. " +
				unmentioned_cas + " entries with \"no mention\" as CAS number were skipped.");
	}

	@Override
	public void migrateLine(String[] line) {
		String inn = line[NAME_INDEX];
		String cas = line[CAS_INDEX];

		if ("no mention".equals(cas)) {
			unmentioned_cas += 1;
		} else if (cas.matches(cas_regex)) {
			addNodes(inn, cas);
		} else {
			System.out.println(
					"Skipping \"" + inn + "\", because CAS is given as \"" + cas + "\", which was not recognized as CAS number.");
		}
	}

	private void addNodes(String inn, String cas) {
		session.run(new Query(
				"CREATE (i:" + INN_LABEL + ":" + CODING_SYSTEM_LABEL + " {code: $inn}) " +
						"MERGE (c:" + CAS_LABEL + ":" + CODING_SYSTEM_LABEL + " {code: $cas}) " +
						"CREATE (i)-[rc:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]->(c)",
				parameters("inn", inn, "cas", cas)));
	}

}
