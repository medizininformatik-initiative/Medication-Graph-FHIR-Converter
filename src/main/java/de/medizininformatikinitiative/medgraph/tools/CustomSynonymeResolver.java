package de.medizininformatikinitiative.medgraph.tools;

import de.medizininformatikinitiative.medgraph.graphdbpopulator.DatabaseConnection;
import de.medizininformatikinitiative.medgraph.gsrsextractor.CSVWriter;
import de.medizininformatikinitiative.medgraph.graphdbpopulator.CSVReader;
import de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders.CustomSynonymeLoader;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.OptionalInt;

import static org.neo4j.driver.Values.parameters;

/**
 * Translates the custom_synonymes_raw.csv file to custom_synonymes.csv.
 * Warning: The target file will be fully overwritten!
 *
 * @author Markus Budeus
 */
public class CustomSynonymeResolver {

	public static void main(String[] args) throws IOException {
		try (DatabaseConnection connection = new DatabaseConnection();
		     Session session = connection.createSession();
		     InputStream inputStream = CustomSynonymeResolver.class.getResourceAsStream(
				     "/custom_synonymes_raw.csv")) {
			CSVReader reader = CSVReader.open(inputStream);
			CSVWriter writer = CSVWriter.open(Path.of("src", "main", "resources", "custom_synonymes.csv"));

			try (inputStream; reader; writer) {
				CustomSynonymeResolver resolver = new CustomSynonymeResolver(session, reader, writer);
				resolver.execute();
			}
		}
	}

	private final Session session;
	private final CSVReader reader;
	private final CSVWriter writer;

	public CustomSynonymeResolver(Session session, CSVReader reader, CSVWriter writer) {
		this.session = session;
		this.reader = reader;
		this.writer = writer;
	}

	private void execute() throws IOException {
		int lineNo = 1;
		reader.readNext();
		String[] line;

		writer.write("Type", "MmiId", "Synonyme");

		while ((line = reader.readNext()) != null) {
			lineNo++;
			String knownSynonyme = line[1];
			String newSynonyme = line[0];
			String type = line[2];

			OptionalInt targetMmiId = resolveMmiIdOf(knownSynonyme, type, lineNo);

			if (targetMmiId.isPresent()) {
				writer.write(type, String.valueOf(targetMmiId.getAsInt()), newSynonyme);
			}
		}
	}

	private OptionalInt resolveMmiIdOf(String synonyme, String type, int lineNo) {
		String targetNodeLabel = CustomSynonymeLoader.resolveLabel(type);
		if (targetNodeLabel == null) {
			System.err.println("Unknown type: " + type + "!");
			return OptionalInt.empty();
		}

		Result result = session.run(new Query(
				"MATCH (t:" + targetNodeLabel + " {name: $known}) " +
						"RETURN t.mmiId",
				parameters("known", synonyme)
		));

		if (!result.hasNext()) {
			System.err.println("Failed to resolve name \"" + synonyme + "\" in line " + lineNo + "!");
			return OptionalInt.empty();
		}
		Record r = result.next();
		if (result.hasNext()) {
			System.err.println("Ambiguous name \"" + synonyme + "\" in line " + lineNo + "!");
			return OptionalInt.empty();
		}
		return OptionalInt.of(r.get(0).asInt());
	}

}
