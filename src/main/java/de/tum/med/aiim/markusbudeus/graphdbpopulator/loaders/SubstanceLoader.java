package de.tum.med.aiim.markusbudeus.graphdbpopulator.loaders;

import org.neo4j.driver.Query;
import org.neo4j.driver.Session;

import java.io.IOException;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.*;

public class SubstanceLoader extends Loader {

	private static final String ID = "ID";
	private static final String NAME = "NAME_PLAIN";
	private static final String ASK = "ASKNUMBER";
	private static final String CAS = "CASREGISTRATIONNUMBER";

	public SubstanceLoader(Session session)
	throws IOException {
		super("MOLECULE.CSV", session);
	}

	@Override
	protected void executeLoad() {
		session.run(new Query(constructLoadStatement(
				"CREATE (s:" + SUBSTANCE_LABEL + " {name: " + row(NAME) + ", mmi_id: " + row(ID) + "}) " +
						"MERGE (a:" + ASK_LABEL + ":" + CODING_SYSTEM_LABEL + " {code: " + row(ASK) + "}) " +
						"MERGE (c:" + CAS_LABEL + ":" + CODING_SYSTEM_LABEL + " {code: " + row(CAS) + "}) " +
						"CREATE (a)-[ra:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]->(s) " +
						"CREATE (c)-[rc:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]->(s)"
		)));
		// Delete ASK and CAS nodes with empty code, since they represent the value being unknown
		session.run(new Query("MATCH (c: " + CAS_LABEL + "{ code: '' }) DETACH DELETE c"));
		session.run(new Query("MATCH (c: " + ASK_LABEL + "{ code: '' }) DETACH DELETE c"));
	}

}
