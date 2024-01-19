package de.tum.med.aiim.markusbudeus.fhirexporter;

import de.tum.med.aiim.markusbudeus.fhirexporter.json.GsonExporter;
import de.tum.med.aiim.markusbudeus.fhirexporter.json.JsonExporter;
import de.tum.med.aiim.markusbudeus.fhirexporter.neo4j.Neo4jExporter;
import de.tum.med.aiim.markusbudeus.fhirexporter.neo4j.Neo4jMedicationExporter;
import de.tum.med.aiim.markusbudeus.fhirexporter.neo4j.Neo4jOrganizationExporter;
import de.tum.med.aiim.markusbudeus.fhirexporter.neo4j.Neo4jSubstanceExporter;
import de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseConnection;
import org.neo4j.driver.Session;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Function;

public class Main {


	// Medications: Statistics{simpleMedicationsTotal=66640, compositeMedicationsTotal=1947, compositeMedicationChildrenTotal=5604, compositeChildObjectsWithAtc=5546, atcOccurrencesInCompositeChildren=5548, simpleObjectsWithAtc=66606, atcOccurrencesInSimpleObjects=66693, objectsWithPzn=68586, pznOccurrences=109458, simpleObjectsWithDoseForm=52564, simpleObjectsWithEdqmDoseForm=52564, compositeChildrenWithDoseForm=4490, compositeChildrenWithEdqmDoseForm=4490}
	// Substances: Statistics{uniiOccurrences=4319, objectsWithUnii=4319, casOccurrences=4740, objectsWithCas=7354, rxcuiOccurrences=3756, objectsWithRxcui=4051, askOccurrences=6361, objectsWithAsk=6361, innOccurrences=3230, objectsWithInn=3230}

	private static final Path OUT_PATH = Path.of("output");
	private static final String SUBSTANCE_OUT_PATH = "substance";
	private static final String MEDICATION_OUT_PATH = "medication";
	private static final String ORGANIZATION_OUT_PATH = "organization";

	public static void main(String[] args) throws IOException {
		try (DatabaseConnection connection = new DatabaseConnection();
		     Session session = connection.createSession()) {
			exportSubstances(session, OUT_PATH.resolve(SUBSTANCE_OUT_PATH), true);
			exportMedications(session, OUT_PATH.resolve(MEDICATION_OUT_PATH), true);
			exportOrganizations(session, OUT_PATH.resolve(ORGANIZATION_OUT_PATH));
		}
	}

	private static String appendPart2UnlessNull(String part1, String part2) {
		if (part2 == null) return part1;
		return part1 + " " + part2;
	}

	public static void exportSubstances(Session session, Path outPath, boolean collectAndPrintStatistics) throws IOException {
		Neo4jSubstanceExporter exporter = new Neo4jSubstanceExporter(session, collectAndPrintStatistics);
		exportToJsonFiles(exporter, outPath,
				substance -> appendPart2UnlessNull(substance.identifier[0].value, substance.description));
		if (collectAndPrintStatistics)
			exporter.printStatistics();
	}
	public static void exportMedications(Session session, Path outPath, boolean collectAndPrintStatistics) throws IOException {
		Neo4jMedicationExporter exporter = new Neo4jMedicationExporter(session, false, collectAndPrintStatistics);
		exportToJsonFiles(exporter, outPath,
				medication -> appendPart2UnlessNull(medication.identifier[0].value,medication.code.text));
		if (collectAndPrintStatistics)
			exporter.printStatistics();
	}
	public static void exportOrganizations(Session session, Path outPath) throws IOException {
		exportToJsonFiles(new Neo4jOrganizationExporter(session), outPath,
				organization -> {
					String name;
					if (organization.alias != null && organization.alias.length > 0) {
						name = organization.alias[0];
					} else name = organization.name;
					return appendPart2UnlessNull(organization.identifier.value, name);
				});
	}

	/**
	 * Exports all objects using the specified exporter and writes them into .json-files.
	 *
	 * @param exporter         the exporter whose objects to convert to json
	 * @param filenameProvider a function which provides a filename for each exported object - the .json-suffix will be
	 *                         appended automatically!
	 */
	private static <T> void exportToJsonFiles(Neo4jExporter<T> exporter, Path outPath,
	                                          Function<T, String> filenameProvider) throws IOException {
		JsonExporter jsonExporter = new GsonExporter(outPath);
		exporter.exportObjects().forEach(object -> {
			try {
				String filename = filenameProvider.apply(object);
				filename = filename.replace(File.separatorChar, '-');

				jsonExporter.writeToJsonFile(filename + ".json", object);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

}
