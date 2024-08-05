package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.nio.file.Path;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;

/**
 * Loads all data from the AMIce file. Requires ASK nodes and Substance nodes to already exist.
 *
 * @author Markus Budeus
 */
public class AmiceStoffBezLoader extends CsvLoader {

	/**
	 * The path within the Neo4j import directory where the file for this loader needs to be.
	 */
	public static final Path RAW_DATA_FILE_PATH = Path.of("amice_stoffbezeichnungen_utf8.csv");

	private static final String ASK = "ASK";
	private static final String PRIMARY_CAS = "CAS";

	private static final String SECONDARY_CAS = "OCAS";

	private static final String INN = "HBEZ1";

	private static final String INN_SOURCE = "INNBEZ1";

	private static final String SYNONYMS = "SYN";

	public AmiceStoffBezLoader(Session session) {
		super(RAW_DATA_FILE_PATH, session);
	}

	@Override
	protected void executeLoad() {

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
						"ON CREATE SET c.source = " + nullIfBlank(row(INN_SOURCE)) + ", c:" + CODE_LABEL + " " +
						"CREATE (c)-[:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]->(s)"
		));

		// Load synonyms
		executeQuery(withLoadStatement(
				"WITH " + ROW_IDENTIFIER + " WHERE size(" + ROW_IDENTIFIER + ") > 15 AND linenumber() > 1 " +
						"MATCH (a:" + ASK_LABEL + " {code: " + row(0) + "})" +
						"-[:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]->(s:" + SUBSTANCE_LABEL + ") " +
						"WITH s, " + ROW_IDENTIFIER + "[16..] + " + row(6) + " + " + row(9) + " + " + row(
						12) + " AS synonyms " +
						"UNWIND synonyms as synonym " +
						"MERGE (c:" + SYNONYM_LABEL + " {name: synonym})" +
						"ON CREATE SET c:TEMP, c.target = [s.mmiId] " +
						"ON MATCH SET c.target = c.target + s.mmiId ",
				DEFAULT_FIELD_TERMINATOR, false)
		);

		// Build synonym relationships
		executeQuery("MATCH (c:" + SYNONYM_LABEL + ":TEMP) " +
				"WITH c, c.target AS targets " +
				"UNWIND targets AS target " +
				"MATCH (s:" + SUBSTANCE_LABEL + " {mmiId: target}) " +
				withRowLimit("WITH c, s CREATE (c)-[:" + SYNONYM_REFERENCES_NODE_LABEL + "]->(s) "));

		// Remove temporary markers
		executeQuery("MATCH (s:" + SYNONYM_LABEL + ":TEMP) " +
				withRowLimit("WITH s " +
						"REMOVE s.target " +
						"REMOVE s:TEMP"));

		// Remove the trash entry
		executeQuery("MATCH (c:" + SYNONYM_LABEL + " {name: '-'}) DETACH DELETE c");

		// Remove duplicate references, which are created if names exist twice in the dataset
		executeQuery(
				"MATCH (s:" + SUBSTANCE_LABEL + ")<-[r1]-(sy:" + SYNONYM_LABEL + ") " +
						"MATCH (s)<-[r2]-(sy) " +
						"WHERE elementId(r1) < elementId(r2) " +
						withRowLimit("WITH r2 DELETE r2")
		);
	}
}
