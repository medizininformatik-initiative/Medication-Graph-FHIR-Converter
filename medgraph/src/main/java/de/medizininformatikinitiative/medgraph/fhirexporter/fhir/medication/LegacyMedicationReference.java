package de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication;

import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Identifier;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Reference;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.Uri;

/**
 * An extension of {@link Reference} meant to be used when referencing a Medication object.
 *
 * @author Markus Budeus
 */
@Deprecated
public class LegacyMedicationReference extends Reference {

	public static final Uri TYPE = new Uri("Medication");

	public LegacyMedicationReference(long parentMmiId, int childNo, Long organizationMmiId) {
		super(TYPE);
		this.identifier = Identifier.combinedMedicalProductSubproductIdentifier(parentMmiId, childNo, organizationMmiId);
	}

}
