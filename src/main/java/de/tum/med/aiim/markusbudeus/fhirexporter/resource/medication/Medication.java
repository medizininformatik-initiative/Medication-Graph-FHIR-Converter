package de.tum.med.aiim.markusbudeus.fhirexporter.resource.medication;

import de.tum.med.aiim.markusbudeus.fhirexporter.resource.Code;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.CodeableConcept;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.Identifier;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.Status;

public class Medication {

	// id omitted
	public Meta meta;
	public Identifier identifier;
	public CodeableConcept code;
	private Code status = new Code(Status.ACTIVE.value);
	// TODO Manufacturer omitted for now
	private CodeableConcept form;
	private Ingredient ingredient;
	// Batch omitted due to not easily doable

	public Code getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		if (status != null) {
			this.status = new Code(status.value);
		} else {
			this.status = null;
		}
	}
}
