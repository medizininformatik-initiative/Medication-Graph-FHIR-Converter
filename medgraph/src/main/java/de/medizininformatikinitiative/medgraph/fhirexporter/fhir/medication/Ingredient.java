package de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication;

import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.CodeableConcept;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Ratio;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Reference;

/**
 * Represents an Ingredient object, which is part of a FHIR Medication.
 *
 * @author Markus Budeus
 */
public class Ingredient {

	// Extension omitted for now
	public Reference itemReference;
	public CodeableConcept itemCodeableConcept;
	public Boolean isActive;
	public Ratio strength;

}
