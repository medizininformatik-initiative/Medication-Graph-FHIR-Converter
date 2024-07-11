package de.medizininformatikinitiative.medgraph.fhirexporter;

import java.nio.file.Path;

/**
 * Main class of the FHIR exporter feature.
 *
 * @author Markus Budeus
 */
public class FhirExporter {

	public FhirExport prepareExport(Path outPath) {
		return new FhirExport(outPath);
	}

}
