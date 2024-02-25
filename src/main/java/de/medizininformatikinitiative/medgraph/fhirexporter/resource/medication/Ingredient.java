package de.medizininformatikinitiative.medgraph.fhirexporter.resource.medication;

import de.medizininformatikinitiative.medgraph.fhirexporter.resource.Ratio;
import de.medizininformatikinitiative.medgraph.fhirexporter.resource.Reference;
import de.medizininformatikinitiative.medgraph.fhirexporter.resource.CodeableConcept;

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
