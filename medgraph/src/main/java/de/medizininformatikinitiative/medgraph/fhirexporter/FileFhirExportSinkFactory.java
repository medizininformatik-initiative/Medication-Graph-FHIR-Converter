package de.medizininformatikinitiative.medgraph.fhirexporter;

import java.nio.file.Path;

/**
 * Factory for {@link FileFhirExportSink}-objects.
 *
 * @author Markus Budeus
 */
@FunctionalInterface
public interface FileFhirExportSinkFactory {

	/**
	 * Prepares a {@link FileFhirExportSink} instance which can be used to orchestrate a single FHIR export.
	 * @param outPath the path to which to export using the returned instance
	 * @return a ready-for-use {@link FileFhirExportSink}-instance
	 */
	FileFhirExportSink prepareExport(Path outPath);

}
