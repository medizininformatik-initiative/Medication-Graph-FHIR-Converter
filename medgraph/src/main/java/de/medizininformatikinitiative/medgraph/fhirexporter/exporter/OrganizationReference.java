package de.medizininformatikinitiative.medgraph.fhirexporter.exporter;

import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.IdProvider;
import org.hl7.fhir.r4.model.Reference;

/**
 * An extension of {@link Reference} meant to be used when referencing an Organization object.
 *
 * @author Markus Budeus
 */
public class OrganizationReference extends Reference {

	public OrganizationReference(long manufacturerMmiId, String manufacturerName) {
		this.setType("Organization");
		this.setReference("Organization/"+ IdProvider.fromOrganizationMmiId(manufacturerMmiId));
		this.setDisplay(manufacturerName);
	}

}
