package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions;
import org.neo4j.driver.Session;

import java.nio.file.Path;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;

/**
 * Loads relevant RxNorm Concepts from the Athena dataset. Includes SCDs and CDFs.
 *
 * @author Markus Budeus
 */
public class AthenaConceptLoader extends CsvLoader {

	private static final String ATHENA_ID = "concept_id";
	private static final String NAME = "concept_name";
	private static final String VOCABULARY = "vocabulary_id";
	private static final String CONCEPT_CLASS = "concept_class_id";
	private static final String CODE = "concept_code";

	public AthenaConceptLoader(Session session) {
		super(Path.of("Athena_CONCEPT.csv"), session);
	}

	@Override
	protected void executeLoad() {
		executeQuery(
				"CREATE CONSTRAINT athenaConceptConstraint IF NOT EXISTS FOR (c:" + ATHENA_LABEL + ") REQUIRE c.athenaId IS UNIQUE"
		);

		startSubtask("Loading CSV");
		executeQuery(withLoadStatement(
				"WITH " + ROW_IDENTIFIER + " WHERE " + row(VOCABULARY) + " = 'RxNorm' AND " +
						"(" + row(CONCEPT_CLASS) + " = 'Clinical Drug Form' OR " + row(
						CONCEPT_CLASS) + " = 'Clinical Drug') " +
						" CREATE (c:" + CODE_LABEL + ":" + RXCUI_LABEL + ":" + ATHENA_LABEL + " {" +
						" code: " + row(CODE) +
						", athenaId: " + row(ATHENA_ID) +
						", name: " + row(NAME) +
						", class: " + row(CONCEPT_CLASS) +
						"})"
				, '\t'));

		startSubtask("Assigning concept class-based labels");
		executeQuery(
				"MATCH (c:" + RXCUI_LABEL + " {class: 'Clinical Drug Form'})" +
						" SET c:" + RXNORM_CDF_LABEL +
						" REMOVE c.class"
		);
		executeQuery(
				"MATCH (c:" + RXCUI_LABEL + " {class: 'Clinical Drug'})" +
						" SET c:" + RXNORM_SCD_LABEL +
						" REMOVE c.class"
		);

		startSubtask("Assigning Athena labels to ATC codes");
		executeQuery(withLoadStatement(
				"WITH " + ROW_IDENTIFIER + " WHERE " + row(VOCABULARY) + " = 'ATC' " +
						"MATCH (a:" + ATC_LABEL + " {code: " + row(CODE) + "})" +
						"SET a:" + ATHENA_LABEL + ", a.athenaId = " + row(ATHENA_ID)
				, '\t'));

	}
}
