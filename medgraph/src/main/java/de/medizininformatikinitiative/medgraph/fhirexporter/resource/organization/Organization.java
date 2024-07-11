package de.medizininformatikinitiative.medgraph.fhirexporter.resource.organization;

import de.medizininformatikinitiative.medgraph.fhirexporter.resource.FhirResource;
import de.medizininformatikinitiative.medgraph.fhirexporter.resource.Identifier;
import de.medizininformatikinitiative.medgraph.fhirexporter.resource.medication.Meta;

/**
 * This class is an implementation of the FHIR R4 Organization object.
 *
 * @author Markus Budeus
 */
public class Organization extends FhirResource {

	public final String resourceType = "Organization";
	public boolean active = true;
	// type omitted
	public String name;
	public String[] alias = new String[0];
	// telecom omitted
	public Address[] address = new Address[0];
	// partOf omitted
	// contact omitted
	// endpoint omitted

}
