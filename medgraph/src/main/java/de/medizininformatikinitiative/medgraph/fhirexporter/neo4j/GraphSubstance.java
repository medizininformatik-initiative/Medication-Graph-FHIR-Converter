package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Identifier;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.substance.Substance;
import org.neo4j.driver.Record;

import java.util.List;

/**
 * @author Markus Budeus
 */
public record GraphSubstance(long mmiId, String name, List<GraphCode> codes) {

	public GraphSubstance(Record record) {
		this(
				record.get(0).asLong(),
				record.get(1).asString(null),
				record.get(2).asList(GraphCode::new)
		);
	}

	public Substance toFhirSubstance() {
		Substance substance = new Substance();
		substance.identifier = new Identifier[] { Identifier.fromSubstanceMmiId(mmiId) };
		substance.description = name;
		substance.code = GraphUtil.toCodeableConcept(codes);
		return substance;
	}

}
