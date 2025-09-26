package de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication;

import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;

/**
 * Implementation of the MII Medication Module "Wirkstofftyp" Extension.
 *
 * @author Markus Budeus
 */
public class ExtensionWirkstoffTyp extends Extension {

	public final String URL = "https://www.medizininformatik-initiative.de/fhir/core/modul-medikation/StructureDefinition/wirkstofftyp";

	private ExtensionWirkstoffTyp() {
		this.setUrl(URL);
	}

	public static ExtensionWirkstoffTyp preciseIngredient() {
		return of("PIN");
	}

	public static ExtensionWirkstoffTyp ingredient() {
		return of("IN");
	}

	private static ExtensionWirkstoffTyp of(String value) {
		ExtensionWirkstoffTyp extension = new ExtensionWirkstoffTyp();
		extension.setValue(new Coding()
				.setCode(value)
				.setSystem("https://www.medizininformatik-initiative.de/fhir/core/modul-medikation/CodeSystem/wirkstofftyp"));
		return extension;
	}

}
