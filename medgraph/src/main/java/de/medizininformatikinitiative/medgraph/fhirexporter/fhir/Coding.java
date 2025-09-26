package de.medizininformatikinitiative.medgraph.fhirexporter.fhir;

import java.util.Objects;

/**
 * This class is an implementation of the FHIR R4 Coding object.
 *
 * @author Markus Budeus
 */
@Deprecated
public class Coding {

	public String system;
	public String version;
	public String code;
	public String display;
	public boolean userSelected = false;

	public void setSystem(Uri system) {
		this.system = system != null ? system.value : null;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		Coding coding = (Coding) object;
		return userSelected == coding.userSelected && Objects.equals(system,
				coding.system) && Objects.equals(version, coding.version) && Objects.equals(code,
				coding.code) && Objects.equals(display, coding.display);
	}

	@Override
	public int hashCode() {
		return Objects.hash(system, version, code, display, userSelected);
	}
}
