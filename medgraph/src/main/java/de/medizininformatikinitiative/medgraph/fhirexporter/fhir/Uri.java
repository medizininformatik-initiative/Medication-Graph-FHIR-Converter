package de.medizininformatikinitiative.medgraph.fhirexporter.fhir;

import java.net.URI;
import java.util.Objects;

/**
 * This class is an implementation of the FHIR R4 uri object.
 *
 * @author Markus Budeus
 */
@Deprecated
public class Uri {

	public final String value;

	public Uri(String value) {
		this.value = value;
	}

	public Uri(URI value) {
		this.value = value.toString();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		Uri uri = (Uri) object;
		return Objects.equals(value, uri.value);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(value);
	}
}
