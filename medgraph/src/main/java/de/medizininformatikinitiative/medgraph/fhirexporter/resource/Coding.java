package de.medizininformatikinitiative.medgraph.fhirexporter.resource;

/**
 * This class is an implementation of the FHIR R4 Coding object.
 *
 * @author Markus Budeus
 */
public class Coding {

	public String system;
	public String version;
	public String code;
	public String display;
	public boolean userSelected = false;

	public void setSystem(Uri system) {
		this.system = system != null ? system.value : null;
	}

}
