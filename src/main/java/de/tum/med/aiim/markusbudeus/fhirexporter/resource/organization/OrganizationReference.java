package de.tum.med.aiim.markusbudeus.fhirexporter.resource.organization;

import de.tum.med.aiim.markusbudeus.fhirexporter.resource.Identifier;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.Reference;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.Uri;

public class OrganizationReference extends Reference {

	public OrganizationReference(long manufacturerMmiId, String manufacturerName) {
		super(new Uri("http://hl7.org/fhir/StructureDefinition/Organization"));
		this.identifier = Identifier.fromMmiId(manufacturerMmiId);
		this.display = manufacturerName;
	}

}
