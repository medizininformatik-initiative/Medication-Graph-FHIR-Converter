package de.medizininformatikinitiative.medgraph.fhirexporter;

import de.medizininformatikinitiative.medgraph.DI;
import de.medizininformatikinitiative.medgraph.common.logging.Level;
import de.medizininformatikinitiative.medgraph.common.logging.LogManager;
import de.medizininformatikinitiative.medgraph.common.logging.Logger;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.FhirResource;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.Medication;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.organization.Organization;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.substance.Substance;
import de.medizininformatikinitiative.medgraph.fhirexporter.json.GsonExporter;
import de.medizininformatikinitiative.medgraph.fhirexporter.json.JsonExporter;
import org.neo4j.driver.Session;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Class which performs the FHIR export routine and writes resources to files.
 *
 * @author Markus Budeus
 */
public class FileFhirExportSink extends FhirExportSink {

	private static final Logger logger = LogManager.getLogger(FileFhirExportSink.class);

	public static final String SUBSTANCE_OUT_PATH = "substance";
	public static final String MEDICATION_OUT_PATH = "medication";
	public static final String ORGANIZATION_OUT_PATH = "organisation";

	private final ExportFilenameGenerator exportFilenameGenerator;

	private final Path outPath;

	/**
	 * Prepares a new FHIR export.
	 *
	 * @param outPath the path into which to write the FHIR objects
	 */
	public FileFhirExportSink(Path outPath) {
		this(outPath, DI.get(ExportFilenameGenerator.class));
	}

	FileFhirExportSink(Path outPath, ExportFilenameGenerator filenameGenerator) {
		super();
		this.exportFilenameGenerator = filenameGenerator;
		this.outPath = outPath;
	}

	@Override
	public void doExport(FhirExportSources sources) throws IOException {
		setProgress(0);
		setMaxProgress(3);

		setTaskStack("Exporting Organizations...");
		exportToJsonFiles(sources.organizationExporter, outPath.resolve(ORGANIZATION_OUT_PATH),
				exportFilenameGenerator::constructFilename);
		setProgress(1);

		setTaskStack("Exporting Substances...");
		exportToJsonFiles(sources.substanceExporter, outPath.resolve(SUBSTANCE_OUT_PATH),
				exportFilenameGenerator::constructFilename);
		setProgress(2);

		setTaskStack("Exporting Medications...");
		exportToJsonFiles(sources.medicationExporter, outPath.resolve(MEDICATION_OUT_PATH),
				exportFilenameGenerator::constructFilename);
		setProgress(3);
	}

	/**
	 * Exports all objects using the specified exporter and writes them into .json-files.
	 *
	 * @param exporter         the exporter whose objects to convert to json
	 * @param filenameProvider a function which provides a filename for each exported object - the .json-suffix will be
	 *                         appended automatically!
	 */
	private <T extends FhirResource> void exportToJsonFiles(FhirExportSource<T> exporter, Path outPath,
	                                                        Function<T, String> filenameProvider) throws IOException {
		if (!outPath.toFile().exists())
			Files.createDirectory(outPath);

		JsonExporter jsonExporter = new GsonExporter(outPath);
		final Set<String> filenamesUsed = ConcurrentHashMap.newKeySet();
		exporter.export().parallel().forEach(object -> {
			try {
				String filename = filenameProvider.apply(object);
				if (!filenamesUsed.add(filename)) {
					logger.log(Level.ERROR, "A filename was generated twice: " + filename);
					return;
				}
				filename = filename.replace(File.separatorChar, '-');

				jsonExporter.writeToJsonFile(filename + ".json", object);
			} catch (IOException e) {
				logger.log(Level.ERROR, "Failed to export FHIR object \"" + Arrays.toString(object.identifier) + "\"",
						e);
			}
		});
	}

}
