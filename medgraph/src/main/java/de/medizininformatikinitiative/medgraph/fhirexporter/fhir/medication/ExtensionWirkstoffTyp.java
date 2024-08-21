package de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication;

import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Coding;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Extension;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Uri;

import java.util.Objects;

/**
 * @author Markus Budeus
 */
public class ExtensionWirkstoffTyp implements Extension {

	public final String url = "https://www.medizininformatik-initiative.de/fhir/core/modul-medikation/StructureDefinition/wirkstofftyp";

	public Coding valueCoding;

	private ExtensionWirkstoffTyp() {

	}

	public static ExtensionWirkstoffTyp preciseIngredient() {
		return of("PIN");
	}

	public static ExtensionWirkstoffTyp ingredient() {
		return of("IN");
	}

	private static ExtensionWirkstoffTyp of(String value) {
		ExtensionWirkstoffTyp extension = new ExtensionWirkstoffTyp();
		extension.valueCoding = new Coding();
		extension.valueCoding.code = value;
		extension.valueCoding.system = "https://www.medizininformatik-initiative.de/fhir/core/modul-medikation/CodeSystem/wirkstofftyp";
		return extension;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		ExtensionWirkstoffTyp that = (ExtensionWirkstoffTyp) object;
		return Objects.equals(valueCoding, that.valueCoding);
	}

	@Override
	public int hashCode() {
		return Objects.hash(valueCoding);
	}

	@Override
	public String toString() {
		return "ExtensionWirkstoffTyp{" + valueCoding.code + '}';
	}
}
