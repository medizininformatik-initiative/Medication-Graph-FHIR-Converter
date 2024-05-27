package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.io.IOException;
import java.nio.file.Path;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;

/**
 * Uses the gsrs_matches.csv file to enrich the database with extracted GSRS data.
 *
 * @author Markus Budeus
 */
public class UniiLoader extends CsvLoader {

	private static final String MMI_ID = "MMIID";
	private static final String UUID = "UUID";
	private static final String NAME = "NAME";
	private static final String UNII = "UNII";
	private static final String RXCUI = "RXCUI";
	private static final String CAS = "CAS";
	private static final String ALTERNATIVE_CAS = "ALTCAS";
	private static final String ALTERNATIVE_RXCUI = "ALTRXCUI";

	public UniiLoader(Session session) {
		super(Path.of("gsrs_matches.csv"), session);
	}

	@Override
	protected void executeLoad() {
		executeQuery(
				"CREATE CONSTRAINT uniiCodeConstraint IF NOT EXISTS FOR (c:" + UNII + ") REQUIRE c.code IS UNIQUE"
		);
		executeQuery(
				"CREATE CONSTRAINT uniiUuidConstraint IF NOT EXISTS FOR (c:" + UNII + ") REQUIRE c.gsrsUuid IS UNIQUE"
		);
		executeQuery(
				"CREATE CONSTRAINT rxcuiCodeConstraint IF NOT EXISTS FOR (c:" + RXCUI + ") REQUIRE c.code IS UNIQUE"
		);

		// Load GSRS UNII, Name and UUID
		executeQuery(withLoadStatement(
				"MATCH (s:" + SUBSTANCE_LABEL + " {mmiId: " + intRow(MMI_ID) + "}) " +
						"MERGE (c:" + UNII_LABEL + " {code: " + row(UNII) + "}) " +
						"ON CREATE SET " +
						"c:" + CODE_LABEL + ", " +
						"c.gsrsUuid = " + row(UUID) + ", " +
						"c.gsrsName = " + row(NAME) + " " +
						"CREATE (c)-[:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]->(s)"
		));
		// Add/Update primary CAS
		executeQuery(withLoadStatement(
				"MATCH (s:" + SUBSTANCE_LABEL + " {mmiId: " + intRow(MMI_ID) + "}) " +
						"MERGE (c:" + CAS_LABEL + " {code: " + row(CAS) + "}) " +
						"ON CREATE SET " +
						"c:" + CODE_LABEL + " " +
						"MERGE (c)-[r:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]->(s) " +
						"ON CREATE SET r.primary = true " +
						"ON MATCH SET r.primary = true"
		));
		// Add/Update secondary CAS
		executeQuery(withLoadStatement(
				"MATCH (s:" + SUBSTANCE_LABEL + " {mmiId: " + intRow(MMI_ID) + "}) " +
						"WITH s, split(" + row(ALTERNATIVE_CAS) + ", '|') as altCas " +
						"WHERE " + nullIfBlank(row(ALTERNATIVE_CAS)) + " IS NOT NULL " +
						"UNWIND altCas as cas " +
						"MERGE (c:" + CAS_LABEL + " {code: cas}) " +
						"ON CREATE SET " +
						"c:" + CODE_LABEL + " " +
						"MERGE (c)-[r:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]->(s) " +
						"ON CREATE SET r.primary = false " +
						"ON MATCH SET r.primary = false"
		));
		// Add primary RXCUI
		executeQuery(withLoadStatement(
				"MATCH (s:" + SUBSTANCE_LABEL + " {mmiId: " + intRow(MMI_ID) + "}) " +
						"WITH s, split(" + row(RXCUI) + ", '|') as primaryRxcui " +
						"WHERE " + nullIfBlank(row(RXCUI)) + " IS NOT NULL " +
						"UNWIND primaryRxcui as rxcui " +
						"MERGE (c:" + RXCUI_LABEL + " {code: rxcui}) " +
						"ON CREATE SET " +
						"c:" + CODE_LABEL + " " +
						"CREATE (c)-[r:" + CODE_REFERENCE_RELATIONSHIP_NAME + " {primary: true}]->(s)"
		));
		// Add secondary RXCUI
		executeQuery(withLoadStatement(
				"MATCH (s:" + SUBSTANCE_LABEL + " {mmiId: " + intRow(MMI_ID) + "}) " +
						"WITH s, split(" + row(ALTERNATIVE_RXCUI) + ", '|') as secondaryRxcui " +
						"WHERE " + nullIfBlank(row(ALTERNATIVE_RXCUI)) + " IS NOT NULL " +
						"WITH s, secondaryRxcui " +
						"UNWIND secondaryRxcui as rxcui " +
						"MERGE (c:" + RXCUI_LABEL + " {code: rxcui}) " +
						"ON CREATE SET " +
						"c:" + CODE_LABEL + " " +
						"CREATE (c)-[r:" + CODE_REFERENCE_RELATIONSHIP_NAME + " {primary: false}]->(s)"
		));
	}
}
