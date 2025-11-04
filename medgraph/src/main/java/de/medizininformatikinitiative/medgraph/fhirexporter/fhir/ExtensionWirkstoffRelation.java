package de.medizininformatikinitiative.medgraph.fhirexporter.fhir;

import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.UriType;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the MII Medication Module "Wirkstoffrelation" Extension.
 *
 * @author Markus Budeus
 */
public class ExtensionWirkstoffRelation {

	public static final String URL = "https://www.medizininformatik-initiative.de/fhir/core/modul-medikation/StructureDefinition/wirkstoffrelation";

	public static class IngredientUri {
		public static final String URL = "ingredientUri";

		private static Extension build(String valueUri) {
			Extension extension = new Extension();
			extension.setUrl(URL);
			extension.setValue(new UriType(valueUri));
			return extension;
		}
	}

	public static Extension relatesTo(String ingredientId) {
		Extension extension = new Extension();
		extension.setUrl(URL);
		List<Extension> list = new ArrayList<>(1);
		list.add(IngredientUri.build(ingredientId));
		extension.setExtension(list);
		return extension;
	}

}
