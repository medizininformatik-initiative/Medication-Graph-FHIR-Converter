package de.tum.med.aiim.markusbudeus.fhirexporter.resource;

public class Reference {

	public final String type;
	public Identifier identifier;
	public String display;

	public Reference(Uri type) {
		this.type = type != null ? type.value : null;
	}

}
