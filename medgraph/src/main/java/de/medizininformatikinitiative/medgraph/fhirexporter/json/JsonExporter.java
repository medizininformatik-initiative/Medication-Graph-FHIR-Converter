package de.medizininformatikinitiative.medgraph.fhirexporter.json;

import org.hl7.fhir.r4.model.Base;

import java.io.IOException;

/**
 * A JsonExporter can write any object in JSON format to a file.
 *
 * @author Markus Budeus
 */
public interface JsonExporter {

	void writeToJsonFile(String filename, Base object) throws IOException;

}
