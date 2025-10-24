package de.medizininformatikinitiative.medgraph.fhirexporter.json;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import de.medizininformatikinitiative.medgraph.DI;
import org.hl7.fhir.r4.model.Base;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Markus Budeus
 */
public class HapiJsonExporter implements JsonExporter {

	private final IParser parser = DI.get(FhirContext.class).newJsonParser();
	private final Path outPath;

	public HapiJsonExporter(Path outPath) throws IOException {
		this.outPath = outPath;
		if (!outPath.toFile().exists()) {
			if (!outPath.toFile().mkdirs()) {
				throw new IOException("Failed to create output directory!");
			}
		} else if (!outPath.toFile().isDirectory()) {
			throw new IOException("The output path does not point to a directory!");
		}
	}

	@Override
	public void writeToJsonFile(String filename, Base object) throws IOException {
		Path path = outPath.resolve(filename);
		Files.writeString(path, parser.encodeToString(object));
	}
}
