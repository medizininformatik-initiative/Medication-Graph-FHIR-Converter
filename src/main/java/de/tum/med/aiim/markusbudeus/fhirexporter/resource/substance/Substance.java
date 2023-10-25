package de.tum.med.aiim.markusbudeus.fhirexporter.resource.substance;

import de.tum.med.aiim.markusbudeus.fhirexporter.resource.Code;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.CodeableConcept;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.Status;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.medication.Ingredient;

public class Substance {

	// identifier omitted
	private Code status;
	public CodeableConcept category;
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
