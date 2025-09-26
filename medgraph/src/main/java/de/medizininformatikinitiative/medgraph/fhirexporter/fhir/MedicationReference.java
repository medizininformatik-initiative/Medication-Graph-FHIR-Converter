package de.medizininformatikinitiative.medgraph.fhirexporter.fhir;

import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.IdProvider;
import org.hl7.fhir.r4.model.Reference;

/**
 * An extension of {@link Reference} meant to be used when referencing a Medication object.
 *
 * @author Markus Budeus
 */
public class MedicationReference extends Reference {

	public MedicationReference(long parentMmiId, int childNo, String medicationName) {
		this.setType("Medication");
		this.setReference("Medication/"+ IdProvider.combinedMedicalProductSubproductIdentifier(parentMmiId, childNo));
		this.setDisplay(medicationName);
	}

}
