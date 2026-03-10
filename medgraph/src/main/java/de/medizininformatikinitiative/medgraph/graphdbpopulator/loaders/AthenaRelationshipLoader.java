package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.nio.file.Path;

import static de.medizininformatikinitiative.medgraph.common.db.DatabaseDefinitions.*;

/**
 * Loads the Athena concept relationships, specifically RxNorm is-a between SCD and CDF and ATC-RxNorm mappings.
 *
 * @author Markus Budeus
 */
public class AthenaRelationshipLoader extends CsvLoader {

	private static final String CONCEPT_1 = "concept_id_1";
	private static final String CONCEPT_2 = "concept_id_2";
	private static final String RELATIONSHIP = "relationship_id";

	public AthenaRelationshipLoader(Session session) {
		super(Path.of("Athena_CONCEPT_RELATIONSHIP.csv"), session);
	}

	@Override
	protected void executeLoad() {
		startSubtask("Loading RxNorm is-a relationships");
		executeQuery(withLoadStatement(
				"WITH " + ROW_IDENTIFIER + " WHERE " + row(RELATIONSHIP) + " = 'RxNorm is a' " +
						"MATCH (c1:" + ATHENA_LABEL + " {athenaId: " + row(CONCEPT_1) + "}) " +
						"MATCH (c2:" + ATHENA_LABEL + " {athenaId: " + row(CONCEPT_2) + "}) " +
						"CREATE (c1)-[:" + RXNORM_IS_A_LABEL + "]->(c2)",
				'\t')
		);
		startSubtask("Loading RxNorm-ATC relationships");
		executeQuery(withLoadStatement(
				"WITH " + ROW_IDENTIFIER + " WHERE " + row(RELATIONSHIP) + " = 'RxNorm - ATC' " +
						"MATCH (c1:" + ATHENA_LABEL + " {athenaId: " + row(CONCEPT_1) + "}) " +
						"MATCH (c2:" + ATHENA_LABEL + " {athenaId: " + row(CONCEPT_2) + "}) " +
						"CREATE (c1)-[:" + RXNORM_CORRESPONDS_TO_ATC_LABEL + "]->(c2)",
				'\t')
		);
	}
}
