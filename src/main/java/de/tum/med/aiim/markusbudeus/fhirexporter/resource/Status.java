package de.tum.med.aiim.markusbudeus.fhirexporter.resource;

public enum Status {
	ACTIVE("active"),
	INACTIVE("inactive"),
	ENTERED_IN_ERROR("entered-in-error");

	public final String value;

	Status(String value) {
		this.value = value;
	}
}
