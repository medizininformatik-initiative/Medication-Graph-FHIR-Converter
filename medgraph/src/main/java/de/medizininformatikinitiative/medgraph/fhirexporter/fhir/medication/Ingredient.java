package de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication;

import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.CodeableConcept;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Ratio;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Reference;

import java.util.Objects;

/**
 * Represents an Ingredient object, which is part of a FHIR Medication.
 *
 * @author Markus Budeus
 */
public class Ingredient {

	// Extension omitted for now
	public Reference itemReference;
	public Boolean isActive;
	public Ratio strength;

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		Ingredient that = (Ingredient) object;
		return Objects.equals(itemReference, that.itemReference) && Objects.equals(isActive,
				that.isActive) && Objects.equals(strength, that.strength);
	}

	@Override
	public int hashCode() {
		return Objects.hash(itemReference, strength);
	}
}
