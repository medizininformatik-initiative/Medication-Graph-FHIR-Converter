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

	private static final Path OUT_PATH = Path.of("output");
	private static final String SUBSTANCE_OUT_PATH = "substance";
	private static final String MEDICATION_OUT_PATH = "medication";
	private static final String ORGANIZATION_OUT_PATH = "organization";

	public static void main(String[] args) throws IOException {
		try (DatabaseConnection connection = new DatabaseConnection();
		     Session session = connection.createSession()) {

			exportToJsonFiles(new Neo4jSubstanceExporter(session), SUBSTANCE_OUT_PATH,
					substance -> appendPart2UnlessNull(substance.identifier[0].value, substance.description));
			exportToJsonFiles(new Neo4jMedicationExporter(session, false), MEDICATION_OUT_PATH,
					medication -> appendPart2UnlessNull(medication.identifier[0].value,medication.code.text));
			exportToJsonFiles(new Neo4jOrganizationExporter(session), ORGANIZATION_OUT_PATH,
					organization -> {
						String name;
						if (organization.alias != null && organization.alias.length > 0) {
							name = organization.alias[0];
						} else name = organization.name;
						return appendPart2UnlessNull(organization.identifier.value, name);
					});
		}
	}

	private static String appendPart2UnlessNull(String part1, String part2) {
		if (part2 == null) return part1;
		return part1 + " " + part2;
	}

	/**
	 * Exports all objects using the specified exporter and writes them into .json-files.
	 *
	 * @param exporter         the exporter whose objects to convert to json
	 * @param outFolder        the folder inside the {@link #OUT_PATH} where to put the files
	 * @param filenameProvider a function which provides a filename for each exported object - the .json-suffix will be
	 *                         appended automatically!
	 */
	private static <T> void exportToJsonFiles(Neo4jExporter<T> exporter, String outFolder,
	                                          Function<T, String> filenameProvider) throws IOException {
		JsonExporter jsonExporter = new GsonExporter(OUT_PATH.resolve(outFolder));
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
