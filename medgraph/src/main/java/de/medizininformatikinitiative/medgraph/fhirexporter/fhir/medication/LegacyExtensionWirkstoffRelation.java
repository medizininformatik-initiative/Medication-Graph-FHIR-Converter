package de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication;

import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Extension;

import java.util.Objects;

/**
 * @author Markus Budeus
 */
@Deprecated
public class LegacyExtensionWirkstoffRelation implements Extension {

	public final String url = "https://www.medizininformatik-initiative.de/fhir/core/modul-medikation/StructureDefinition/wirkstoffrelation";
	public IngredientUri[] extension = new IngredientUri[] { new IngredientUri() };

	public static class IngredientUri implements Extension {
		public final String url = "ingredientUri";
		public String valueUri;
	}

	public static LegacyExtensionWirkstoffRelation relatesTo(String ingredientId) {
		LegacyExtensionWirkstoffRelation extension = new LegacyExtensionWirkstoffRelation();
		extension.extension[0].valueUri = ingredientId;
		return extension;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		LegacyExtensionWirkstoffRelation that = (LegacyExtensionWirkstoffRelation) object;
		return Objects.equals(extension[0].valueUri, that.extension[0].valueUri);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(extension[0].valueUri);
	}

	@Override
	public String toString() {
		return "ExtensionWirkstoffRelation{" + extension[0].valueUri + '}';
	}
}
