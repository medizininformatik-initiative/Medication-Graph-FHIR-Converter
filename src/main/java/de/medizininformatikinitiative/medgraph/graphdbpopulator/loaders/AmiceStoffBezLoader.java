package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.io.IOException;
import java.nio.file.Path;

import static de.medizininformatikinitiative.medgraph.graphdbpopulator.DatabaseDefinitions.*;

/**
 * Loads all data from the AMIce file. Requires ASK nodes and Substance nodes to already exist.
 *
 * @author Markus Budeus
 */
public class AmiceStoffBezLoader extends CsvLoader {

	private static final String ASK = "ASK";
	private static final String PRIMARY_CAS = "CAS";

	private static final String SECONDARY_CAS = "OCAS";

	private static final String INN = "HBEZ1";

	private static final String INN_SOURCE = "INNBEZ1";

	private static final String SYNONYMES = "SYN";

	public AmiceStoffBezLoader(Session session) throws IOException {
		super(Path.of("amice_stoffbezeichnungen_utf8.csv"), session);
	}

	@Override
	protected void executeLoad() {
		executeQuery(
				"CREATE CONSTRAINT casCodeConstraint IF NOT EXISTS FOR (c:" + CAS_LABEL + ") REQUIRE c.code IS UNIQUE");
		executeQuery(
				"CREATE CONSTRAINT innCodeConstraint IF NOT EXISTS FOR (i:" + INN_LABEL + ") REQUIRE i.code IS UNIQUE");
		executeQuery(
				"CREATE CONSTRAINT synonymeConstraint IF NOT EXISTS FOR (s:" + SYNONYME_LABEL + ") REQUIRE s.name IS UNIQUE");

		// Load primary CAS
		executeQuery(withLoadStatement(
				"WITH " + ROW_IDENTIFIER + " WHERE " + nullIfBlank(row(PRIMARY_CAS)) + " IS NOT NULL " +
						"MATCH (a:" + ASK_LABEL + " {code: " + row(ASK) + "})" +
						"-[:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]->" +
						"(s:" + SUBSTANCE_LABEL + ") " +
						"MERGE (c:" + CAS_LABEL + " {code: " + row(PRIMARY_CAS) + "}) " +
						"ON CREATE SET c:" + CODE_LABEL + " " +
						"MERGE (c)-[r:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]->(s) " +
						"ON CREATE SET r.primary = true " +
						"ON MATCH SET r.primary = true"
		));

		// Load secondary CAS
		executeQuery(withLoadStatement(
				"WITH " + ROW_IDENTIFIER + " WHERE " + nullIfBlank(row(SECONDARY_CAS)) + " IS NOT NULL " +
						"MATCH (a:" + ASK_LABEL + " {code: " + row(ASK) + "})" +
						"-[:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]->" +
						"(s:" + SUBSTANCE_LABEL + ") " +
						"WITH s, split(" + row(SECONDARY_CAS) + ",'|') AS secondaryCasList " +
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

		// Load synonymes
		executeQuery(withLoadStatement(
				"WITH " + ROW_IDENTIFIER + " WHERE size(" + ROW_IDENTIFIER + ") > 15 AND linenumber() > 1 " +
						"MATCH (a:" + ASK_LABEL + " {code: " + row(0) + "})" +
						"-[:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]->(s:" + SUBSTANCE_LABEL + ") " +
						"WITH s, " + ROW_IDENTIFIER + "[16..] + " + row(6) + " + " + row(9) + " + " + row(
						12) + " AS synonymes " +
						"UNWIND synonymes as synonyme " +
						"MERGE (c:" + SYNONYME_LABEL + " {name: synonyme})" +
						"ON CREATE SET c:TEMP, c.target = [s.mmiId] " +
						"ON MATCH SET c.target = c.target + s.mmiId ",
				DEFAULT_FIELD_TERMINATOR, false)
		);

		// Build synonyme relationships
		executeQuery("MATCH (c:" + SYNONYME_LABEL + ":TEMP) " +
				"WITH c, c.target AS targets " +
				"UNWIND targets AS target " +
				"MATCH (s:" + SUBSTANCE_LABEL + " {mmiId: target}) " +
				"CREATE (c)-[:" + SYNONYME_REFERENCES_NODE_LABEL + "]->(s) ");

		// Remove temporary markers
		executeQuery("MATCH (s:" + SYNONYME_LABEL + ":TEMP) " +
				"REMOVE s.target " +
				"REMOVE s:TEMP");

		// Remove the trash entry
		executeQuery("MATCH (c:" + SYNONYME_LABEL + " {name: '-'}) DETACH DELETE c");

		// Remove duplicate references, which are created if names exist twice in the dataset
		executeQuery(
				"MATCH (s:"+SUBSTANCE_LABEL+")<-[r1]-(sy:"+SYNONYME_LABEL+") " +
						"MATCH (s)<-[r2]-(sy) " +
						"WHERE elementId(r1) < elementId(r2) " +
						"DELETE r2"
		);
	}
}
