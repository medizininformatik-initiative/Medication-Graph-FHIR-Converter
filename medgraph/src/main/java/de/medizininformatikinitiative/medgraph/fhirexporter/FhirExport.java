package de.medizininformatikinitiative.medgraph.fhirexporter;

import de.medizininformatikinitiative.medgraph.common.mvc.NamedProgressableImpl;
import de.medizininformatikinitiative.medgraph.fhirexporter.json.GsonExporter;
import de.medizininformatikinitiative.medgraph.fhirexporter.json.JsonExporter;
import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.Neo4jExporter;
import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.Neo4jMedicationExporter;
import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.Neo4jOrganizationExporter;
import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.Neo4jSubstanceExporter;
import de.medizininformatikinitiative.medgraph.fhirexporter.resource.medication.Medication;
import de.medizininformatikinitiative.medgraph.fhirexporter.resource.organization.Organization;
import de.medizininformatikinitiative.medgraph.fhirexporter.resource.substance.Substance;
import org.neo4j.driver.Session;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Class which performs the FHIR export routine. Create a new instance and call {@link #doExport(Session)} to start
 * the export.
 *
 * @author Markus Budeus
 */
public class FhirExport extends NamedProgressableImpl {

	private static final int ITERATIONS_PER_PROGRESS_UPDATE = 200;

	public static final String SUBSTANCE_OUT_PATH = "substance";
	public static final String MEDICATION_OUT_PATH = "medication";
	public static final String ORGANIZATION_OUT_PATH = "organisation";

	private final Path outPath;
	private final AtomicInteger atomicProgress = new AtomicInteger();

	/**
	 * Prepares a new FHIR export.
	 * @param outPath the path into which to write the FHIR objects
	 */
	public FhirExport(Path outPath) {
		super();
		this.outPath = outPath;
	}

	public void doExport(Session session) throws IOException {
		Neo4jOrganizationExporter organizationExporter = new Neo4jOrganizationExporter(session);
		Neo4jSubstanceExporter substanceExporter = new Neo4jSubstanceExporter(session, false);
		Neo4jMedicationExporter medicationExporter = new Neo4jMedicationExporter(session, false, false);

		setTaskStack("Gathering statistics...");
		setLocalProgress(0);

		int organizationObjects = organizationExporter.countObjects();
		int substanceObjects = substanceExporter.countObjects();
		int medicationObjects = medicationExporter.countObjects();

		setMaxProgress(organizationObjects + substanceObjects + medicationObjects);

		setTaskStack("Exporting Organizations...");
		exportToJsonFiles(organizationExporter, outPath.resolve(ORGANIZATION_OUT_PATH), this::constructOrganizationFilename);
		setLocalProgress(organizationObjects);

		setTaskStack("Exporting Substances...");
		exportToJsonFiles(substanceExporter, outPath.resolve(SUBSTANCE_OUT_PATH), this::constructSubstanceFilename);
		setLocalProgress(organizationObjects + substanceObjects);

		setTaskStack("Exporting Medications...");
		exportToJsonFiles(medicationExporter, outPath.resolve(MEDICATION_OUT_PATH), this::constructMedicationFilename);
		setLocalProgress(organizationObjects + substanceObjects + medicationObjects);
	}

	/**
	 * Exports all objects using the specified exporter and writes them into .json-files. Also updates the progress
	 * every {@link #ITERATIONS_PER_PROGRESS_UPDATE} objects. However, the progress is incremented by one for each
	 * object.
	 *
	 * @param exporter         the exporter whose objects to convert to json
	 * @param filenameProvider a function which provides a filename for each exported object - the .json-suffix will be
	 *                         appended automatically!
	 */
	private <T> void exportToJsonFiles(Neo4jExporter<T> exporter, Path outPath,
	                                   Function<T, String> filenameProvider) throws IOException {
		if (!outPath.toFile().exists())
			Files.createDirectory(outPath);

		JsonExporter jsonExporter = new GsonExporter(outPath);
		final Set<String> filenamesUsed = new HashSet<>();
		AtomicInteger iteration = new AtomicInteger(0);
		exporter.exportObjects().forEach(object -> {
			try {
				String filename = filenameProvider.apply(object);
				if (!filenamesUsed.add(filename)) {
					throw new IllegalArgumentException("A filename was generated twice: " + filename);
				}
				filename = filename.replace(File.separatorChar, '-');

				jsonExporter.writeToJsonFile(filename + ".json", object);
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				if (iteration.addAndGet(1) % ITERATIONS_PER_PROGRESS_UPDATE == 0) {
					addLocalProgress(ITERATIONS_PER_PROGRESS_UPDATE);
				}
			}
		});
	}

	private static String combine(String... parts) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String s : parts) {
			if (s != null) {
				if (first)
					first = false;
				else
					sb.append(" ");
				sb.append(s);
			}
		}
		return sb.toString();
	}

	private void setLocalProgress(int progress) {
		atomicProgress.set(progress);
		this.setProgress(progress);
	}

	private void addLocalProgress(int progress) {
		this.setProgress(atomicProgress.addAndGet(progress));
	}

	private String constructOrganizationFilename(Organization organization) {
		String name;
		if (organization.alias != null && organization.alias.length > 0) {
			name = organization.alias[0];
		} else name = organization.name;
		return combine(organization.identifier.value, name);
	}

	private String constructSubstanceFilename(Substance substance) {
		return combine(substance.identifier[0].value, substance.description);
	}

	private String constructMedicationFilename(Medication medication) {
		return combine(medication.identifier[0].value, medication.code.text);
	}

}
