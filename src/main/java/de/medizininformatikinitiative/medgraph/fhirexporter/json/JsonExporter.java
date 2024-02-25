package de.medizininformatikinitiative.medgraph.fhirexporter.json;

import java.io.IOException;

/**
 * A JsonExporter can write any object in JSON format to a file.
 *
 * @author Markus Budeus
 */
public interface JsonExporter {

	void writeToJsonFile(String filename, Object object) throws IOException;

}
