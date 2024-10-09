package de.medizininformatikinitiative.medgraph.fhirexporter;

import de.medizininformatikinitiative.medgraph.DI;
import de.medizininformatikinitiative.medgraph.common.logging.Level;
import de.medizininformatikinitiative.medgraph.common.logging.LogManager;
import de.medizininformatikinitiative.medgraph.common.logging.Logger;
import de.medizininformatikinitiative.medgraph.common.mvc.NamedProgressableImpl;
import de.medizininformatikinitiative.medgraph.fhirexporter.exporter.GraphFhirExportSource;
import de.medizininformatikinitiative.medgraph.fhirexporter.exporter.Neo4jOrganizationExporter;
import de.medizininformatikinitiative.medgraph.fhirexporter.exporter.Neo4jProductExporter;
import de.medizininformatikinitiative.medgraph.fhirexporter.exporter.Neo4jSubstanceExporter;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.FhirResource;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.medication.Medication;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.organization.Organization;
import de.medizininformatikinitiative.medgraph.fhirexporter.fhir.substance.Substance;
import de.medizininformatikinitiative.medgraph.fhirexporter.json.GsonExporter;
import de.medizininformatikinitiative.medgraph.fhirexporter.json.JsonExporter;
import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.GraphOrganization;
import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.GraphSubstance;
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
 * Class which performs the FHIR export routine. Create a new instance and call {@link #doExport(Session)} to start the
 * export.
 *
 * @author Markus Budeus
 */
public class FhirExport extends NamedProgressableImpl {

	private static final Logger logger = LogManager.getLogger(FhirExport.class);

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
	public FhirExport(Path outPath) {
		this(outPath, DI.get(ExportFilenameGenerator.class));
	}

	FhirExport(Path outPath, ExportFilenameGenerator filenameGenerator) {
		super();
		this.exportFilenameGenerator = filenameGenerator;
		this.outPath = outPath;
	}

	/**
	 * Runs the FHIR export pipeline using the given Neo4j database session.
	 */
	public void doExport(Session session) throws IOException {
		FhirExportSource<Organization> organizationExporter = new GraphFhirExportSource<>(
				new Neo4jOrganizationExporter(session),
				s -> s.map(GraphOrganization::toFhirOrganization));
		FhirExportSource<Substance> substanceExporter = new GraphFhirExportSource<>(new Neo4jSubstanceExporter(session),
				s -> s.map(GraphSubstance::toFhirSubstance));
		FhirExportSource<Medication> medicationExporter = new GraphFhirExportSource<>(
				new Neo4jProductExporter(session, false),
				s -> s.flatMap(p -> p.toFhirMedications().stream()));

		doExport(organizationExporter, substanceExporter, medicationExporter);
	}

	/**
	 * Runs the FHIR export pipeline using the given exporter instances. External calls into this method should be made
	 * for testing only.
	 */
	void doExport(FhirExportSource<Organization> organizationExporter,
	              FhirExportSource<Substance> substanceExporter,
	              FhirExportSource<Medication> medicationExporter) throws IOException {
		setProgress(0);
		setMaxProgress(3);

		setTaskStack("Exporting Organizations...");
		exportToJsonFiles(organizationExporter, outPath.resolve(ORGANIZATION_OUT_PATH),
				exportFilenameGenerator::constructFilename);
		setProgress(1);

		setTaskStack("Exporting Substances...");
		exportToJsonFiles(substanceExporter, outPath.resolve(SUBSTANCE_OUT_PATH),
				exportFilenameGenerator::constructFilename);
		setProgress(2);

		setTaskStack("Exporting Medications...");
		exportToJsonFiles(medicationExporter, outPath.resolve(MEDICATION_OUT_PATH),
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
