package de.medizininformatikinitiative.medgraph.fhirexporter.fhir;

import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.IdProvider;
import org.hl7.fhir.r4.model.Reference;

/**
 * @author Markus Budeus
 */
public class MedicationReference {

	public static Reference build(long parentMmiId, int childNo, String medicationName) {
		return new Reference()
				.setType("Medication")
				.setReference(
						"Medication/" + IdProvider.combinedMedicalProductSubproductIdentifier(parentMmiId, childNo))
				.setDisplay(medicationName);
	}

}
