package de.tum.med.aiim.markusbudeus.fhirexporter.resource.organization;

import de.tum.med.aiim.markusbudeus.fhirexporter.resource.Identifier;

public class Organization {

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
