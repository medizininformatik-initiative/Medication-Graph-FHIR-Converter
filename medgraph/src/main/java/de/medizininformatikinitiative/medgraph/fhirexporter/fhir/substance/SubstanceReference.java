package de.medizininformatikinitiative.medgraph.fhirexporter.fhir.substance;

import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Identifier;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Reference;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Uri;

/**
 * An extension of {@link Reference} meant to be used when referencing a Substance object.
 *
 * @author Markus Budeus
 */
public class SubstanceReference extends Reference {

	public SubstanceReference(long substanceMmiId, String substanceName) {
		super(new Uri("http://hl7.org/fhir/StructureDefinition/Substance"));
		this.identifier = Identifier.fromSubstanceMmiId(substanceMmiId);
		this.display = substanceName;
	}

}
