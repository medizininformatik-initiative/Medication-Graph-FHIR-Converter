package de.medizininformatikinitiative.medgraph.fhirexporter.fhir.organization;

import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Identifier;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Reference;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Uri;

/**
 * An extension of {@link Reference} meant to be used when referencing an Organization object.
 *
 * @author Markus Budeus
 */
public class OrganizationReference extends Reference {

	public OrganizationReference(long manufacturerMmiId, String manufacturerName) {
		super(new Uri("Organization"));
		this.identifier = Identifier.fromOrganizationMmiId(manufacturerMmiId);
		this.display = manufacturerName;
	}

}
