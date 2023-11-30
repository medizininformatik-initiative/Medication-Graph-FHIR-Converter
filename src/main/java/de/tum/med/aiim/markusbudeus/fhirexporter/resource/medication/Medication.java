package de.tum.med.aiim.markusbudeus.fhirexporter.resource.medication;

import de.tum.med.aiim.markusbudeus.fhirexporter.resource.*;

public class Medication {

	// id omitted
	public final String resourceType = "Medication";
	public Meta meta;
	public Identifier[] identifier;
	public CodeableConcept code;
	private String status = Status.ACTIVE.value;
	public Reference manufacturer;
	public CodeableConcept form;
	public Ratio amount;
	public Ingredient[] ingredient;
	// Batch omitted due to not easily doable

	public String getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		if (status != null) {
			this.status = status.value;
		} else {
			this.status = null;
		}
	}
}
