package de.medizininformatikinitiative.medgraph.fhirexporter.fhir;

import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;

/**
 * Implementation of the MII Medication Module "Wirkstofftyp" Extension.
 *
 * @author Markus Budeus
 */
public class ExtensionWirkstoffTyp {

	public static final String URL = "https://www.medizininformatik-initiative.de/fhir/core/modul-medikation/StructureDefinition/wirkstofftyp";

	public static Extension preciseIngredient() {
		return of("PIN");
	}

	public static Extension ingredient() {
		return of("IN");
	}

	private static Extension of(String value) {
		Extension extension = new Extension();
		extension.setUrl(URL);
		extension.setValue(new Coding()
				.setCode(value)
				.setSystem("https://www.medizininformatik-initiative.de/fhir/core/modul-medikation/CodeSystem/wirkstofftyp"));
		return extension;
	}

}
