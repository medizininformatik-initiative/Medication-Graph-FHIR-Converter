package de.tum.med.aiim.markusbudeus.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.io.IOException;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.*;

/**
 * Reads the ITEM_ATC.CSV file to connect drug nodes to the ATC nodes.
 */
public class DrugAtcConnectionLoader extends CsvLoader {


	private static final String ITEM_ID = "ITEMID";
	private static final String ATC_CODE = "ATCCODE";

	public DrugAtcConnectionLoader(Session session) throws IOException {
		super("ITEM_ATC.CSV", session);
	}

	@Override
	protected void executeLoad() {
		executeQuery(withLoadStatement(
				"MATCH (d:" + DRUG_LABEL + " {mmiId: " + intRow(ITEM_ID) + "}) " +
						"MATCH (a:" + ATC_LABEL + " {code: " + row(ATC_CODE) + "}) " +
						"WITH a, d " +
						"CREATE (d)-[:" + DRUG_MATCHES_ATC_CODE_LABEL + "]->(a)"
		));
	}
}
