package de.tum.med.aiim.markusbudeus.fhirexporter.resource.substance;

import de.tum.med.aiim.markusbudeus.fhirexporter.resource.CodeableConcept;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.Identifier;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.Status;

public class Substance {

	public final String resourceType = "Substance";
	public Identifier[] identifier;
	private String  status = Status.ACTIVE.value;
	public CodeableConcept[] category;
	/**
	 * A code (or set of codes) that identify this substance.
	 * May not be null.
	 */
	public CodeableConcept code;
	public String description;
	// Instance omitted
	// Ingredient omitted

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
