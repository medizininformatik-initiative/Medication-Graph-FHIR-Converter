package de.tum.med.aiim.markusbudeus.fhirexporter.resource;

import java.net.URI;

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
