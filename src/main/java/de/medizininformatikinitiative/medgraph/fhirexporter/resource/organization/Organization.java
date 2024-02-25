package de.medizininformatikinitiative.medgraph.fhirexporter.resource.organization;

import de.medizininformatikinitiative.medgraph.fhirexporter.resource.Identifier;

/**
 * This class is an implementation of the FHIR R4 Organization object.
 *
 * @author Markus Budeus
 */
public class Organization {

	public final String resourceType = "Organization";
	public Identifier identifier;
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
