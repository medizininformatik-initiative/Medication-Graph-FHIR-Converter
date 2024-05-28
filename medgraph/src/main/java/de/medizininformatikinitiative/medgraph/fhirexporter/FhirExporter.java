package de.medizininformatikinitiative.medgraph.fhirexporter;

import de.medizininformatikinitiative.medgraph.fhirexporter.json.GsonExporter;
import de.medizininformatikinitiative.medgraph.fhirexporter.json.JsonExporter;
import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.Neo4jExporter;
import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.Neo4jMedicationExporter;
import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.Neo4jOrganizationExporter;
import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.Neo4jSubstanceExporter;
import org.neo4j.driver.Session;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/**
 * Main class of the FHIR exporter feature. Contains the main logic for exporting the knowledge graph data into files.
 * When invoked via its main method, it exports all substances, medications and organizations to the {@link #OUT_PATH}.
 *
 * @author Markus Budeus
 */
public class FhirExporter {

	public static final Path OUT_PATH = Path.of("output");
	public static final String SUBSTANCE_OUT_PATH = "substance";
	public static final String MEDICATION_OUT_PATH = "medication";
	public static final String ORGANIZATION_OUT_PATH = "organisation";

	public void exportAll(Session session) throws IOException {
		exportSubstances(session, OUT_PATH.resolve(SUBSTANCE_OUT_PATH), true);
		exportMedications(session, OUT_PATH.resolve(MEDICATION_OUT_PATH), true);
		exportOrganizations(session, OUT_PATH.resolve(ORGANIZATION_OUT_PATH));
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

	public void exportSubstances(Session session, Path outPath, boolean collectAndPrintStatistics)
	throws IOException {
		Neo4jSubstanceExporter exporter = new Neo4jSubstanceExporter(session, collectAndPrintStatistics);
		exportToJsonFiles(exporter, outPath,
				substance -> combine(substance.identifier[0].value, substance.description));
		if (collectAndPrintStatistics)
			exporter.printStatistics();
	}

	public void exportMedications(Session session, Path outPath, boolean collectAndPrintStatistics)
	throws IOException {
		Neo4jMedicationExporter exporter = new Neo4jMedicationExporter(session, false, collectAndPrintStatistics);
		exportToJsonFiles(exporter, outPath,
				medication -> combine(
						medication.identifier[0].value,
						medication.code.text
				));
		if (collectAndPrintStatistics)
			exporter.printStatistics();
	}

	public void exportOrganizations(Session session, Path outPath) throws IOException {
		exportToJsonFiles(new Neo4jOrganizationExporter(session), outPath,
				organization -> {
					String name;
					if (organization.alias != null && organization.alias.length > 0) {
						name = organization.alias[0];
					} else name = organization.name;
					return combine(organization.identifier.value, name);
				});
	}

	/**
	 * Exports all objects using the specified exporter and writes them into .json-files.
	 *
	 * @param exporter         the exporter whose objects to convert to json
	 * @param filenameProvider a function which provides a filename for each exported object - the .json-suffix will be
	 *                         appended automatically!
	 */
	private <T> void exportToJsonFiles(Neo4jExporter<T> exporter, Path outPath,
	                                   Function<T, String> filenameProvider) throws IOException {
		JsonExporter jsonExporter = new GsonExporter(outPath);
		final Set<String> filenamesUsed = new HashSet<>();
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
			}
		});
	}

}
