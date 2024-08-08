package de.medizininformatikinitiative.medgraph.fhirexporter.fhir;

/**
 * This class is an implementation of the FHIR R4 Reference object.
 *
 * @author Markus Budeus
 */
public class Reference {

	public final String type;
	public Identifier identifier;
	public String display;

	public Reference(Uri type) {
		this.type = type != null ? type.value : null;
	}

}
