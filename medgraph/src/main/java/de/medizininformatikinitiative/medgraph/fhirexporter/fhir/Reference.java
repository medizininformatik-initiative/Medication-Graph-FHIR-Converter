package de.medizininformatikinitiative.medgraph.fhirexporter.fhir;

import java.util.Objects;

/**
 * This class is an implementation of the FHIR R4 Reference object.
 *
 * @author Markus Budeus
 */
@Deprecated
public class Reference {

	public final String type;
	public Identifier identifier;
	public String display;

	public Reference(Uri type) {
		this.type = type != null ? type.value : null;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		Reference reference = (Reference) object;
		return Objects.equals(type, reference.type) && Objects.equals(identifier,
				reference.identifier) && Objects.equals(display, reference.display);
	}

	@Override
	public int hashCode() {
		return Objects.hash(identifier, display);
	}
}
