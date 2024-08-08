package de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication;

import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Coding;

/**
 * Represents an Meta object, which is part of a FHIR Medication.
 *
 * @author Markus Budeus
 */
public class Meta {

	// versionId omitted
	// lastUpdated omitted
	public String source;
	public String[] profile;
	// Security omitted
	public Coding tag;

}
