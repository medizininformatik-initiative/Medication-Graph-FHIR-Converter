package de.tum.med.aiim.markusbudeus.fhirexporter.neo4j;

import de.tum.med.aiim.markusbudeus.fhirexporter.resource.CodeableConcept;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.Coding;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.Identifier;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.substance.Substance;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseDefinitions.*;

public class Neo4jSubstanceExporter extends Neo4jExporter<Substance> {

	private static final String CODE = "code";
	private static final String SYSTEM_URI = "uri";
	private static final String SYSTEM_DATE = "date";
	private static final String SYSTEM_VERSION = "version";

	public Neo4jSubstanceExporter(Session session) {
		super(session);
	}

	/**
	 * Reads all substances with their assigned codes and coding systems from the database and returns them as a stream
	 * of {@link Substance Substances}.
	 */
	@Override
	public Stream<Substance> exportObjects() {
		Result result = session.run(new Query(
				"MATCH (s:" + SUBSTANCE_LABEL + " {name: 'Midazolam hydrochlorid'}) " +
						"MATCH (cs:" + CODING_SYSTEM_LABEL + ")<-[:" + BELONGS_TO_CODING_SYSTEM_LABEL + "]-(c:" + CODE_LABEL + ")-->(s) " +
						"WITH s, collect({" +
						CODE + ":c.code," +
						SYSTEM_URI + ":cs.uri," +
						SYSTEM_DATE + ":cs.date," +
						SYSTEM_VERSION + ":cs.version" +
						"}) AS codes " +
						"RETURN s.name, s.mmiId, codes"
		));

		return result.stream().map(Neo4jSubstanceExporter::toSubstance);

	}

	private static Substance toSubstance(Record record) {
		Substance substance = new Substance();

		substance.identifier = new Identifier[] { Identifier.fromSubstanceMmiId(record.get(1).asLong()) };
		substance.description = record.get(0).asString(); // Substance name

		CodeableConcept codeableConcept = new CodeableConcept();
		List<Coding> codings = record.get(2).asList(code -> CodingProvider.createCoding(
						code.get(CODE).asString(),
						(String) code.get(SYSTEM_URI).asObject(),
						(String) code.get(SYSTEM_VERSION).asObject(),
						(LocalDate) code.get(SYSTEM_DATE).asObject()
				)
		);
		codeableConcept.coding = codings.toArray(new Coding[0]);

		substance.code = codeableConcept;

		return substance;
	}

}
