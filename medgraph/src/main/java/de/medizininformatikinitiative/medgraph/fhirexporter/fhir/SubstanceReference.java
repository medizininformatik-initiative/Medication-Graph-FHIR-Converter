package de.medizininformatikinitiative.medgraph.fhirexporter.fhir;

import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.IdProvider;
import org.hl7.fhir.r4.model.Reference;

/**
 * An extension of {@link Reference} meant to be used when referencing a Substance object.
 *
 * @author Markus Budeus
 */
public class SubstanceReference extends Reference {

	public SubstanceReference(long substanceMmiId, String substanceName) {
		this.setType("Substance");
		this.setReference("Substance/"+ IdProvider.fromSubstanceMmiId(substanceMmiId));
		this.setDisplay(substanceName);
	}

}
