package de.medizininformatikinitiative.medgraph.fhirexporter.json;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A {@link JsonExporter}-implementation using Google Gson.
 *
 * @author Markus Budeus
 */
public class GsonExporter implements JsonExporter {

	private final Gson gson = new Gson();
	private final Path outPath;

	public GsonExporter(Path outPath) throws IOException {
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
	public void writeToJsonFile(String filename, Object object) throws IOException {
		Path path = outPath.resolve(filename);
		Files.writeString(path, gson.toJson(object));
	}

}
