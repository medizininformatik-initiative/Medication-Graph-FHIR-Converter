package de.tum.med.aiim.markusbudeus.fhirexporter.resource.substance;

import de.tum.med.aiim.markusbudeus.fhirexporter.resource.Identifier;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.Uri;

public class SubstanceReference {

	public final Uri type = new Uri("http://hl7.org/fhir/StructureDefinition/Substance");
	public Identifier identifier;
	public String display;

	public SubstanceReference(long substanceMmiId, String substanceName) {
		this.identifier = Identifier.fromMmiId(substanceMmiId);
		this.display = substanceName;
	}

}
