package de.tum.med.aiim.markusbudeus.fhirexporter.json;

import java.io.IOException;

public interface JsonExporter {

	void writeToJsonFile(String filename, Object object) throws IOException;

}
