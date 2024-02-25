package de.medizininformatikinitiative.medgraph.fhirexporter.resource.medication;

import de.medizininformatikinitiative.medgraph.fhirexporter.resource.Coding;

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
