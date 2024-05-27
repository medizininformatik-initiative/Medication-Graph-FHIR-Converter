package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions;
import org.neo4j.driver.Session;

import java.io.IOException;

/**
 * Reads the ITEM_ATC.CSV file to connect drug nodes to the ATC nodes.
 *
 * @author Markus Budeus
 */
public class DrugAtcConnectionLoader extends CsvLoader {


	private static final String ITEM_ID = "ITEMID";
	private static final String ATC_CODE = "ATCCODE";

	public DrugAtcConnectionLoader(Session session) {
		super("ITEM_ATC.CSV", session);
	}

	@Override
	protected void executeLoad() {
		executeQuery(withLoadStatement(
				"MATCH (d:" + DatabaseDefinitions.DRUG_LABEL + " {mmiId: " + intRow(ITEM_ID) + "}) " +
						"MATCH (a:" + DatabaseDefinitions.ATC_LABEL + " {code: " + row(ATC_CODE) + "}) " +
						"WITH a, d " +
						"CREATE (d)-[:" + DatabaseDefinitions.DRUG_MATCHES_ATC_CODE_LABEL + "]->(a)"
		));
	}
}
