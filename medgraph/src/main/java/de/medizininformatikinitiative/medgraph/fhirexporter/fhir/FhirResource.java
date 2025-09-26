package de.medizininformatikinitiative.medgraph.fhirexporter.fhir;

import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.Meta;

/**
 * @author Markus Budeus
 */
@Deprecated
public class FhirResource {
	public Meta meta;
	public Identifier[] identifier;
}
