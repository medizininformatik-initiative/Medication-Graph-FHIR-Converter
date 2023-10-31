package de.tum.med.aiim.markusbudeus.fhirexporter.resource.medication;

import de.tum.med.aiim.markusbudeus.fhirexporter.resource.CodeableConcept;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.RatioOrQuantity;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.substance.SubstanceReference;

public class Item {

	public CodeableConcept itemCodeableConcept;
	public SubstanceReference itemReference;
	public Boolean isActive;
	public RatioOrQuantity strength;

}
