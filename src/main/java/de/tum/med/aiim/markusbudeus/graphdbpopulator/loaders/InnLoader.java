package de.tum.med.aiim.markusbudeus.graphdbpopulator.loaders;

import org.neo4j.driver.Query;
import org.neo4j.driver.Session;

import java.io.IOException;
import java.nio.file.Path;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.*;

public class InnLoader extends Loader {

	private static final String INN = "PRODUCT_EN";
	public static final String CAS = "CAS";

	public InnLoader(Session session) throws IOException {
		super(Path.of("inn_list.csv"), session);
	}

	@Override
	protected void executeLoad() {
		session.run(new Query(
				"CREATE CONSTRAINT innCodeConstraint IF NOT EXISTS FOR (i:" + INN_LABEL + ") REQUIRE i.code IS UNIQUE"
		));
		session.run(new Query(withLoadStatement(
				"CREATE (i:" + INN_LABEL + ":" + CODING_SYSTEM_LABEL + " {code: " + row(INN) + "}) " +
						"MERGE (c:" + CAS_LABEL + " {code: " + row(CAS) + "}) " +
						"ON CREATE SET c:" + CODING_SYSTEM_LABEL + " " +
						"CREATE (i)-[:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]->(c)"
		)));
	}
}
