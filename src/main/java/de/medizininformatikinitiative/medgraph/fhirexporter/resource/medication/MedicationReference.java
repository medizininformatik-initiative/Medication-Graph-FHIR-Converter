package de.medizininformatikinitiative.medgraph.fhirexporter.resource.medication;

import de.medizininformatikinitiative.medgraph.fhirexporter.resource.Reference;
import de.medizininformatikinitiative.medgraph.fhirexporter.resource.Uri;
import de.medizininformatikinitiative.medgraph.fhirexporter.resource.Identifier;

/**
 * An extension of {@link Reference} meant to be used when referencing a Medication object.
 *
 * @author Markus Budeus
 */
public class MedicationReference extends Reference {

	public static final Uri TYPE = new Uri("http://hl7.org/fhir/StructureDefinition/Medication");

	public MedicationReference(long parentMmiId, int childNo, Long organizationMmiId) {
		super(TYPE);
		this.identifier = Identifier.combinedMedicalProductSubproductIdentifier(parentMmiId, childNo, organizationMmiId);
	}

}
