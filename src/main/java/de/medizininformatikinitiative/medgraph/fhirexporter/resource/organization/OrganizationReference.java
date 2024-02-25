package de.medizininformatikinitiative.medgraph.fhirexporter.resource.organization;

import de.medizininformatikinitiative.medgraph.fhirexporter.resource.Reference;
import de.medizininformatikinitiative.medgraph.fhirexporter.resource.Identifier;
import de.medizininformatikinitiative.medgraph.fhirexporter.resource.Uri;

/**
 * An extension of {@link Reference} meant to be used when referencing an Organization object.
 *
 * @author Markus Budeus
 */
public class OrganizationReference extends Reference {

	public OrganizationReference(long manufacturerMmiId, String manufacturerName) {
		super(new Uri("http://hl7.org/fhir/StructureDefinition/Organization"));
		this.identifier = Identifier.fromOrganizationMmiId(manufacturerMmiId);
		this.display = manufacturerName;
	}

}
