package de.medizininformatikinitiative.medgraph.fhirexporter.resource.medication;

import de.medizininformatikinitiative.medgraph.fhirexporter.resource.*;

/**
 * This class is an implementation of the "Basismodul Medikation (2023)"'s Medication object.
 *
 * @author Markus Budeus
 */
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
