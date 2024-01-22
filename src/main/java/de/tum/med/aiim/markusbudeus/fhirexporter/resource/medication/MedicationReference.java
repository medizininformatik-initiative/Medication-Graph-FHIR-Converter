package de.tum.med.aiim.markusbudeus.fhirexporter.resource.medication;

import de.tum.med.aiim.markusbudeus.fhirexporter.resource.Identifier;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.Reference;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.Uri;

public class MedicationReference extends Reference {

	public static final Uri TYPE = new Uri("http://hl7.org/fhir/StructureDefinition/Medication");

	public MedicationReference(long parentMmiId, int childNo, Long organizationMmiId) {
		super(TYPE);
		this.identifier = Identifier.combinedMedicalProductSubproductIdentifier(parentMmiId, childNo, organizationMmiId);
	}

}
