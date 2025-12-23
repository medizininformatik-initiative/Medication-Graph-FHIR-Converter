package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions;
import org.neo4j.driver.Session;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.ARCHIVED_ATTR;

/**
 * This class creates the archived product nodes in the database using the ARCHIVE_PRODUCT table from the
 * MMI Pharmindex. This must be executed after products and archived companies have been loaded.
 *
 * @author Markus Budeus
 */
public class ArchiveProductLoader extends CsvLoader {

	private static final String ID = "ID";
	private static final String NAME = "NAME";
	private static final String PHARMACEUTICAL_FLAG = "PHARMACEUTICAL_FLAG";

	public ArchiveProductLoader(Session session) {
		super("ARCHIVE_PRODUCT.CSV", session);
	}

	@Override
	protected void executeLoad() {
		// In case a non-archived product with that same id exists, it wins.
		executeQuery(withLoadStatement(
				"WITH "+ROW_IDENTIFIER+" WHERE " + row(PHARMACEUTICAL_FLAG) + " = '1'"+
				" MERGE (d:" + DatabaseDefinitions.PRODUCT_LABEL + " { mmiId: "+intRow(ID)+ " })" +
						// Add names but remove HTML <sub> and <sup> tags. No other HTML tags seem to exist in the names.
						" ON CREATE SET name: replace(replace(replace(replace(" + row(NAME) +", '<sub>', ''), '</sub>', ''), '<sup>', ''), '</sup>', '')" +
						" SET " + ARCHIVED_ATTR + ": true"
		));
	}
}
