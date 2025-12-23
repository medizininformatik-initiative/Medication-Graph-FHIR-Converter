package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.ARCHIVED_ATTR;
import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.COMPANY_LABEL;

/**
 * Creates company/manufacturer nodes using the archived company csv.
 * This must be executed after the regular companies have been loaded.
 *
 * @author Markus Budeus
 */
public class ArchiveCompanyLoader extends CsvLoader {

	// TODO Test

	private static final String ID = "ID";
	private static final String NAME = "NAME";

	public ArchiveCompanyLoader(Session session) {
		super("ARCHIVE_COMPANY.CSV", session);
	}

	@Override
	protected void executeLoad() {
		// In case a non-archived company with that same id exists, it wins.
		executeQuery(withLoadStatement(
				"MERGE (c:" + COMPANY_LABEL +
						" { mmiId: " + intRow(ID) + "})" +
						" ON CREATE SET name: " + nullIfBlank(row(NAME)) +
						" SET " + ARCHIVED_ATTR + ": true"
		));
	}
}
