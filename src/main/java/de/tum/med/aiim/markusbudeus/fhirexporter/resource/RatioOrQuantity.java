package de.tum.med.aiim.markusbudeus.fhirexporter.resource;

public interface RatioOrQuantity {


	// TODO StructureDefinition claims Quantity shall be used if the denominator of a ratio is known to be 1.
	// This, however, conflicts with the examples in
	// https://simplifier.net/guide/mii-ig-medikation-v2024/MIIIGModulMedikation-2.x/TechnischeImplementierung-2.x/FHIR-Profile-2.x/Medication-2.x?version=current

	/**
	 * Adds this ratio or quantity to another one. If both are of the same type and the units match, the sum of
	 * both is returned. If the action fails due to different units, null is returned.
	 */
	RatioOrQuantity plus(RatioOrQuantity other);

}