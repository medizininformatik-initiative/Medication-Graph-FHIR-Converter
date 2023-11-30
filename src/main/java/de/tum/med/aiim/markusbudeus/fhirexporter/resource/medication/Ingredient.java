package de.tum.med.aiim.markusbudeus.fhirexporter.resource.medication;

import de.tum.med.aiim.markusbudeus.fhirexporter.resource.CodeableConcept;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.Ratio;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.RatioOrQuantity;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.Reference;

public class Ingredient {

	// TODO Extension omitted for now
	public Reference itemReference;
	public CodeableConcept itemCodeableConcept;
	public Boolean isActive;
	public Ratio strength;

}
