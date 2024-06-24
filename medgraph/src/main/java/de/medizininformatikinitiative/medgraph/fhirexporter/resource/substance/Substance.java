package de.medizininformatikinitiative.medgraph.fhirexporter.resource.substance;

import de.medizininformatikinitiative.medgraph.fhirexporter.resource.CodeableConcept;
import de.medizininformatikinitiative.medgraph.fhirexporter.resource.Identifier;
import de.medizininformatikinitiative.medgraph.fhirexporter.resource.Status;
import de.medizininformatikinitiative.medgraph.fhirexporter.resource.medication.Meta;

/**
 * This class is an implementation of the FHIR R4 Substance object.
 *
 * @author Markus Budeus
 */
public class Substance {

	public Meta meta;
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
