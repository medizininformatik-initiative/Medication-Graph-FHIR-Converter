package de.tum.med.aiim.markusbudeus.fhirexporter.resource;

import java.net.URI;

public class Uri {

	public final String value;

	public Uri(String value) {
		this.value = value;
	}

	public Uri(URI value) {
		this.value = value.toString();
	}

}
