package de.tum.med.aiim.markusbudeus.fhirexporter.resource.medication;

import de.tum.med.aiim.markusbudeus.fhirexporter.resource.Identifier;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.Reference;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.Uri;

public class MedicationReference extends Reference {

	public MedicationReference(long parentMmiId, int childNo) {
		super(new Uri("http://hl7.org/fhir/StructureDefinition/Medication"));
		this.identifier = Identifier.combinedMedicalProductSubproductIdentifier(parentMmiId, childNo);
	}

}
