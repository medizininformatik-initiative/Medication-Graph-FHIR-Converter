package de.tum.med.aiim.markusbudeus.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.io.IOException;
import java.nio.file.Path;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.*;

/**
 * Creates INN nodes and references to CAS nodes. CAS Nodes are created as necessary, but will be resolved if they
 * already exist.
 */
public class InnLoader extends CsvLoader {

	private static final String INN = "PRODUCT_EN";
	public static final String CAS = "CAS";

	public InnLoader(Session session) throws IOException {
		super(Path.of("inn_list.csv"), session);
	}

	@Override
	protected void executeLoad() {
		executeQuery(
				"CREATE CONSTRAINT innCodeConstraint IF NOT EXISTS FOR (i:" + INN_LABEL + ") REQUIRE i.code IS UNIQUE"
		);
		executeQuery(withLoadStatement(
				"WITH " + ROW_IDENTIFIER + " WHERE NOT " + row(CAS) + " = '' " +
						"CREATE (i:" + INN_LABEL + ":" + CODE_LABEL + " {code: " + row(INN) + "}) " +
						"MERGE (c:" + CAS_LABEL + " {code: " + row(CAS) + "}) " +
						"ON CREATE SET c:" + CODE_LABEL + " " +
						"CREATE (i)-[:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]->(c)"
		));
	}
}
