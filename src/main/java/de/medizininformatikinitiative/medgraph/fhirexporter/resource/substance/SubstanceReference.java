package de.medizininformatikinitiative.medgraph.fhirexporter.resource.substance;

import de.medizininformatikinitiative.medgraph.fhirexporter.resource.Identifier;
import de.medizininformatikinitiative.medgraph.fhirexporter.resource.Reference;
import de.medizininformatikinitiative.medgraph.fhirexporter.resource.Uri;

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
