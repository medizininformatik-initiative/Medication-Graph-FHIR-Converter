package de.medizininformatikinitiative.medgraph.fhirexporter.fhir;

import java.net.URI;

/**
 * This class is an implementation of the FHIR R4 uri object.
 *
 * @author Markus Budeus
 */
public class Uri {

	public final String value;

	public Uri(String value) {
		this.value = value;
	}

	public Uri(URI value) {
		this.value = value.toString();
	}

}
