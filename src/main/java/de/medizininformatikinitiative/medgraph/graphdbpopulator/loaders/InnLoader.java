package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import de.medizininformatikinitiative.medgraph.graphdbpopulator.DatabaseDefinitions;
import org.neo4j.driver.Session;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Creates INN nodes and references to CAS nodes using the inn_list.csv.
 * CAS Nodes are created as necessary, but will be resolved if they already exist.
 *
 * @author Markus Budeus
 * @deprecated INNs are currently being resolved using the AMIce data set
 */
@Deprecated
public class InnLoader extends CsvLoader {

	private static final String INN = "PRODUCT_EN";
	public static final String CAS = "CAS";

	public InnLoader(Session session) throws IOException {
		super(Path.of("inn_list.csv"), session);
	}

	@Override
	protected void executeLoad() {
		executeQuery(
				"CREATE CONSTRAINT innCodeConstraint IF NOT EXISTS FOR (i:" + DatabaseDefinitions.INN_LABEL + ") REQUIRE i.code IS UNIQUE"
		);
		executeQuery(withLoadStatement(
				"WITH " + ROW_IDENTIFIER + " WHERE NOT " + row(CAS) + " = '' " +
						"CREATE (i:" + DatabaseDefinitions.INN_LABEL + ":" + DatabaseDefinitions.CODE_LABEL + " {code: " + row(INN) + "}) " +
						"MERGE (c:" + DatabaseDefinitions.CAS_LABEL + " {code: " + row(CAS) + "}) " +
						"ON CREATE SET c:" + DatabaseDefinitions.CODE_LABEL + " " +
						"CREATE (i)-[:" + DatabaseDefinitions.CODE_REFERENCE_RELATIONSHIP_NAME + "]->(c)"
		));
	}
}
