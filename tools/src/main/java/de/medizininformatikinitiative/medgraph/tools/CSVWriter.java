package de.medizininformatikinitiative.medgraph.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;

/**
 * Interface used to write to a CSV file.
 *
 * @author Markus Budeus
 */
public interface CSVWriter extends AutoCloseable {

	static CSVWriter open(Path path) throws IOException {
		return CSVWriter.open(path.toFile());
	}

	static CSVWriter open(File file) throws IOException {
		return CSVWriter.open(new FileWriter(file));
	}

	static CSVWriter open(Writer writer) throws IOException {
		return CSVWriterImpl.open(writer);
	}

	/**
	 * Writes the given elements as next line into the CSV file.
	 */
	void write(String... line);

	void close() throws IOException;

}
