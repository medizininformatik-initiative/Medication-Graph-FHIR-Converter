package de.medizininformatikinitiative.medgraph.fhirexporter.fhir.substance;

import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.CodeableConcept;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.FhirResource;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Status;

/**
 * This class is an implementation of the FHIR R4 Substance object.
 *
 * @author Markus Budeus
 */
public class Substance extends FhirResource {

	public final String resourceType = "Substance";
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
