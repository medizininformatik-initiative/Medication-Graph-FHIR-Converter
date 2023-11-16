package de.tum.med.aiim.markusbudeus.fhirexporter.resource.substance;

import de.tum.med.aiim.markusbudeus.fhirexporter.resource.Identifier;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.Reference;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.Uri;

import java.sql.Ref;

public class SubstanceReference extends Reference {

	public SubstanceReference(long substanceMmiId, String substanceName) {
		super(new Uri("http://hl7.org/fhir/StructureDefinition/Substance"));
		this.identifier = Identifier.fromSubstanceMmiId(substanceMmiId);
		this.display = substanceName;
	}

}
