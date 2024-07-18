package de.medizininformatikinitiative.medgraph.fhirexporter;

import java.nio.file.Path;

/**
 * Factory for {@link FhirExport}-objects.
 *
 * @author Markus Budeus
 */
@FunctionalInterface
public interface FhirExportFactory {

	/**
	 * Prepares a {@link FhirExport} instance which can be used to orchestrate a single FHIR export.
	 * @param outPath the path to which to export using the returned instance
	 * @return a ready-for-use {@link FhirExport}-instance
	 */
	FhirExport prepareExport(Path outPath);

}
