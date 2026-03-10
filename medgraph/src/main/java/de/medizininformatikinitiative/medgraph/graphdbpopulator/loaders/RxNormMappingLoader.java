package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.nio.file.Path;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;

/**
 * @author Markus Budeus
 */
public class RxNormMappingLoader extends CsvLoader {

	private static final String MMI_ID = "MMI_DRUG_ID";
	private static final String SCD_RXCUI = "SCD_RXCUI";

	public RxNormMappingLoader(Session session) {
		super(Path.of("rxnorm_mapping.csv"), session);
	}

	@Override
	protected void executeLoad() {
		executeQuery(withLoadStatement(
				"MATCH (d:"+DRUG_LABEL+" {mmiId: "+intRow(MMI_ID)+"}) " +
						"MATCH (scd:"+RXCUI_LABEL+":"+RXNORM_SCD_LABEL+" {code: "+row(SCD_RXCUI)+"}) " +
						"CREATE (d)-[:"+DRUG_MATCHES_SCD_LABEL+"]->(scd)"
		));
	}
}
