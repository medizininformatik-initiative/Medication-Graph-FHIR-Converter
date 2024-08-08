package de.medizininformatikinitiative.medgraph.fhirexporter.fhir;

/**
 * An enumeration of the different status used, e.g., for the Substance.status field.
 *
 * @author Markus Budeus
 */
public enum Status {
	ACTIVE("active"),
	INACTIVE("inactive"),
	ENTERED_IN_ERROR("entered-in-error");

	public final String value;

	Status(String value) {
		this.value = value;
	}
}
