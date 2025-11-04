package de.medizininformatikinitiative.medgraph.fhirexporter.fhir;

import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.IdProvider;
import org.hl7.fhir.r4.model.Reference;

/**
 * @author Markus Budeus
 */
public class SubstanceReference {

	public static Reference build(long substanceMmiId, String substanceName) {
		return new Reference()
				.setType("Substance")
				.setReference("Substance/" + IdProvider.fromSubstanceMmiId(substanceMmiId))
				.setDisplay(substanceName);
	}

}
