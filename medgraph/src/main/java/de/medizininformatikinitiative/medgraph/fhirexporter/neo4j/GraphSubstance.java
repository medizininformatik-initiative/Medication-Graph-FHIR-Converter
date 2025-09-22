package de.medizininformatikinitiative.medgraph.fhirexporter.neo4j;

import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Identifier;
import org.hl7.fhir.r4.model.Substance;
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

	public de.medizininformatikinitiative.medgraph.fhirexporter.fhir.substance.Substance toLegacyFhirSubstance() {
		de.medizininformatikinitiative.medgraph.fhirexporter.fhir.substance.Substance substance =
				new de.medizininformatikinitiative.medgraph.fhirexporter.fhir.substance.Substance();
		substance.identifier = new Identifier[] { Identifier.fromSubstanceMmiId(mmiId) };
		substance.code = GraphUtil.toLegacyCodeableConcept(codes);
		substance.code.text = name;
		return substance;
	}

	public Substance toFhirSubstance() {
		Substance substance = new Substance();
		substance.setId(IdProvider.fromSubstanceMmiId(mmiId));
		substance.setCode(GraphUtil.toCodeableConcept(codes));
		substance.getCode().setText(name);
		return substance;
	}

}
