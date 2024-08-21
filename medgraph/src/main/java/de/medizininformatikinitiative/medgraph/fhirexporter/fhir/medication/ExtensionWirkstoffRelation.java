package de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication;

import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Extension;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Uri;

import java.util.Objects;

/**
 * @author Markus Budeus
 */
public class ExtensionWirkstoffRelation implements Extension {

	public final String url = "https://www.medizininformatik-initiative.de/fhir/core/modul-medikation/StructureDefinition/wirkstoffrelation";
	public IngredientUri extension = new IngredientUri();

	public static class IngredientUri implements Extension {
		public final String url = "ingredientUri";
		public String valueUri;
	}

	public static ExtensionWirkstoffRelation relatesTo(String ingredientId) {
		ExtensionWirkstoffRelation extension = new ExtensionWirkstoffRelation();
		extension.extension.valueUri = ingredientId;
		return extension;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		ExtensionWirkstoffRelation that = (ExtensionWirkstoffRelation) object;
		return Objects.equals(extension.valueUri, that.extension.valueUri);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(extension.valueUri);
	}

	@Override
	public String toString() {
		return "ExtensionWirkstoffRelation{" + extension.valueUri + '}';
	}
}
