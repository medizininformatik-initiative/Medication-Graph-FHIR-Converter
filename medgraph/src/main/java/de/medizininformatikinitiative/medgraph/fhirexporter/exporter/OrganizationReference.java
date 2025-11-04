package de.medizininformatikinitiative.medgraph.fhirexporter.exporter;

import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.IdProvider;
import org.hl7.fhir.r4.model.Reference;

/**
 * @author Markus Budeus
 */
public class OrganizationReference {

	public static Reference build(long manufacturerMmiId, String manufacturerName) {
		return new Reference()
				.setType("Organization")
				.setReference("Organization/" + IdProvider.fromOrganizationMmiId(manufacturerMmiId))
				.setDisplay(manufacturerName);
	}

}
