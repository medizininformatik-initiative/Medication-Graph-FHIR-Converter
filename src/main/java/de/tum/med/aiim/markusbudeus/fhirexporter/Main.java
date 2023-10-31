package de.tum.med.aiim.markusbudeus.fhirexporter;

import de.tum.med.aiim.markusbudeus.fhirexporter.json.GsonExporter;
import de.tum.med.aiim.markusbudeus.fhirexporter.json.JsonExporter;
import de.tum.med.aiim.markusbudeus.fhirexporter.neo4j.Neo4jSubstanceExporter;
import de.tum.med.aiim.markusbudeus.fhirexporter.resource.substance.Substance;
import de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseConnection;
import org.neo4j.driver.Session;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

public class Main {

	private static final Path OUT_PATH = Path.of("output");
	private static final String SUBSTANCE_OUT_PATH = "substance";

	public static void main(String[] args) throws IOException {
		try (DatabaseConnection connection = new DatabaseConnection();
		     Session session = connection.createSession()) {
			Neo4jSubstanceExporter neo4jExporter = new Neo4jSubstanceExporter(session);

			Stream<Substance> exportStream = neo4jExporter.loadSubstances();

			JsonExporter substanceJsonExporter = new GsonExporter(OUT_PATH.resolve(SUBSTANCE_OUT_PATH));
			exportStream.forEach(substance -> {
				try {
					substanceJsonExporter.writeToJsonFile(substance.identifier.value + ".json", substance);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
		}
	}

}
