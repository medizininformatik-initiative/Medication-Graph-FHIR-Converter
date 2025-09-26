package de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication;

import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Extension;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Ratio;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Reference;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents an Ingredient object, which is part of a FHIR Medication.
 *
 * @author Markus Budeus
 */
@Deprecated
public class Ingredient {

	public String id = null;
	public Extension[] extension = null;
	public Reference itemReference;
	public Boolean isActive;
	public Ratio strength;

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		Ingredient that = (Ingredient) object;
		return Objects.equals(id, that.id)
				&& Objects.deepEquals(extension, that.extension)
				&& Objects.equals(itemReference, that.itemReference)
				&& Objects.equals(isActive, that.isActive)
				&& Objects.equals(strength, that.strength);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, itemReference, strength);
	}

	@Override
	public String toString() {
		return "Ingredient{" +
				"id='" + id + '\'' +
				", extension=" + Arrays.toString(extension) +
				", itemReference=" + itemReference +
				", isActive=" + isActive +
				", strength=" + strength +
				'}';
	}
}
