package de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication;

import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.UriType;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the MII Medication Module "Wirkstoffrelation" Extension.
 *
 * @author Markus Budeus
 */
public class ExtensionWirkstoffRelation extends Extension {

	public final String URL = "https://www.medizininformatik-initiative.de/fhir/core/modul-medikation/StructureDefinition/wirkstoffrelation";

	private ExtensionWirkstoffRelation() {
		this.setUrl(URL);
	}

	public static class IngredientUri extends Extension {
		public final String URL = "ingredientUri";

		private IngredientUri(String valueUri) {
			this.setUrl(URL);
			this.value = new UriType(valueUri);
		}
	}

	public void setRelatesTo(String ingredientId) {
		List<Extension> list = new ArrayList<>(1);
		list.add(new IngredientUri(ingredientId));
		this.setExtension(list);
	}

	public static ExtensionWirkstoffRelation relatesTo(String ingredientId) {
		ExtensionWirkstoffRelation extension = new ExtensionWirkstoffRelation();
		extension.setRelatesTo(ingredientId);
		return extension;
	}

}
