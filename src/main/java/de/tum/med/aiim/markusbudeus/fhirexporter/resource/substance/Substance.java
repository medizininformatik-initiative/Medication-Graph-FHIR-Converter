package de.tum.med.aiim.markusbudeus.fhirexporter.resource.substance;

import de.tum.med.aiim.markusbudeus.fhirexporter.resource.Code;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.CodeableConcept;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.Status;

public class Substance {

	// identifier omitted
	private Code status = new Code(Status.ACTIVE.value);
	public CodeableConcept[] category = new CodeableConcept[0];
	/**
	 * A code (or set of codes) that identify this substance.
	 * May not be null.
	 */
	public CodeableConcept code;
	public String description;
	// Instance omitted
	// Ingredient omitted

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
