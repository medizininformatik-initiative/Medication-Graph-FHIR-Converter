package de.medizininformatikinitiative.medgraph.fhirexporter.fhir.organization;

import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.FhirResource;

/**
 * This class is an implementation of the FHIR R4 Organization object.
 *
 * @author Markus Budeus
 */
@Deprecated
public class Organization extends FhirResource {

	public final String resourceType = "Organization";
	public boolean active = true;
	// type omitted
	public String name;
	public String[] alias = new String[0];
	// telecom omitted
	public FhirAddress[] address = new FhirAddress[0];
	// partOf omitted
	// contact omitted
	// endpoint omitted

}
