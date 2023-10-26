package de.tum.med.aiim.markusbudeus.fhirexporter.neo4j;

import de.tum.med.aiim.markusbudeus.fhirexporter.resource.CodeableConcept;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.Coding;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.substance.Substance;
import de.tum.med.aiim.markusbudeus.graphdbpopulator.CodingSystem;
import de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseConnection;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.util.ArrayList;
import java.util.List;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.*;

public class Neo4jSubstanceExporter {

	public static void main(String[] args) {
		try (DatabaseConnection connection = new DatabaseConnection();
		     Session session = connection.createSession()) {

			Result result = session.run(new Query(
					"MATCH (s:" + SUBSTANCE_LABEL + " {name: 'Acetylsalicylsäure'}) " +
							"OPTIONAL MATCH (a:" + ASK_LABEL + ")-[:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]->(s) " +
							"OPTIONAL MATCH (c:" + CAS_LABEL + ")-[:" + CODE_REFERENCE_RELATIONSHIP_NAME + "]->(s) " +
							"RETURN s.name, a.code, c.code"
			));

			// MATCH (s:Substance {name: 'Midazolam'}) OPTIONAL MATCH (cs:CodingSystem)--(c:Code)-[:REFERENCES]->(s) RETURN s,collect({suri:cs.uri,sname:cs.name,sdate:cs.date, snotice:cs.notice,code:c.code})

			Substance substance = toSubstance(result.next());


		}
	}

	private static Substance toSubstance(Record record) {
		Substance substance = new Substance();
		substance.description = record.get(0).asString(); // Substance name
		CodeableConcept codeableConcept = new CodeableConcept();
		List<Coding> codings = new ArrayList<>(2);

		// TODO Somehow include the notice
		addCodingIfNotNull(record.get(1).asString(), CodingSystem.ASK, codings);
//		addCodingIfNotNull(record.get(2).asString(), CodingSystem.CAS, codings);

		codeableConcept.coding = codings.toArray(new Coding[0]);

		substance.code = codeableConcept;

		return substance;
	}

	private static void addCodingIfNotNull(String coding, CodingSystem codingSystem, List<Coding> list) {

	}

}
