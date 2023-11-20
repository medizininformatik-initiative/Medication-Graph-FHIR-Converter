package de.tum.med.aiim.markusbudeus.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.io.IOException;
import java.nio.file.Path;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.*;

public class AmiceStoffBezLoader extends CsvLoader {

	private static final String ASK = "ASK";
	private static final String PRIMARY_CAS = "CAS";

	private static final String SECONDARY_CAS = "OCAS";

	private static final String INN = "HBEZ1";

	private static final String INN_SOURCE = "INNBEZ1";

	public AmiceStoffBezLoader(Session session) throws IOException {
		super(Path.of("amice_stoffbezeichnungen_utf8.csv"), session);
	}

	@Override
	protected void executeLoad() {
		executeQuery(
				"CREATE CONSTRAINT casCodeConstraint IF NOT EXISTS FOR (c:" + CAS_LABEL + ") REQUIRE c.code IS UNIQUE");
		executeQuery(
				"CREATE CONSTRAINT innCodeConstraint IF NOT EXISTS FOR (i:" + INN_LABEL + ") REQUIRE i.code IS UNIQUE");

		// Load primary CAS
		executeQuery(withLoadStatement(
				"WITH " + ROW_IDENTIFIER + " WHERE " + nullIfBlank(row(PRIMARY_CAS)) + " IS NOT NULL " +
						"MATCH (a:" + ASK_LABEL + " {code: " + row(ASK) + "})" +
						"-[:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]->" +
						"(s:" + SUBSTANCE_LABEL + ") " +
						"MERGE (c:" + CAS_LABEL + " {code: " + row(PRIMARY_CAS) + "}) " +
						"ON CREATE SET c:" + CODE_LABEL + " " +
						"CREATE (c)-[:" + CODE_REFERENCE_RELATIONSHIP_NAME + " {primary: true}]->(s)"
		));

		// Load secondary CAS
		executeQuery(withLoadStatement(
				"WITH " + ROW_IDENTIFIER + " WHERE " + nullIfBlank(row(SECONDARY_CAS)) + " IS NOT NULL " +
						"MATCH (a:" + ASK_LABEL + " {code: " + row(ASK) + "})" +
						"-[:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]->" +
						"(s:" + SUBSTANCE_LABEL + ") " +
						"WITH s, split("+row(SECONDARY_CAS)+",'|') AS secondaryCasList " +
						"UNWIND secondaryCasList AS secondaryCas " +
						"MERGE (c:" + CAS_LABEL + " {code: secondaryCas}) " +
						"ON CREATE SET c:" + CODE_LABEL + " " +
						"CREATE (c)-[:" + CODE_REFERENCE_RELATIONSHIP_NAME + " {primary: false}]->(s)"
		));

		// Load INN
		executeQuery(withLoadStatement(
				"WITH " + ROW_IDENTIFIER + " WHERE " + nullIfBlank(row(INN)) + " IS NOT NULL " +
						"AND " + row(INN) + " <> '-' " +
						"MATCH (a:" + ASK_LABEL + " {code: " + row(ASK) + "})" +
						"-[:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]->(s:" + SUBSTANCE_LABEL + ") " +
						"MERGE (c:" + INN_LABEL + " {code: " + row(INN) + "}) " +
						"ON CREATE SET c.source = " + row(INN_SOURCE) + ", c:" + CODE_LABEL + " " +
						"CREATE (c)-[:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]->(s)"
		));

	}
}
