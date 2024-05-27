package de.medizininformatikinitiative.medgraph.tools;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;

/**
 * Wrapper around whichever CSV writing library is used.
 *
 * @author Markus Budeus
 */
public class CSVWriter implements AutoCloseable {

	public static CSVWriter open(Path path) throws IOException {
		return CSVWriter.open(path.toFile());
	}

	public static CSVWriter open(File file) throws IOException {
		return CSVWriter.open(new FileWriter(file));
	}

	public static CSVWriter open(Writer writer) throws IOException {
		ICSVWriter icsvWriter = new CSVWriterBuilder(writer)
				.withSeparator(';')
				.build();
		return new CSVWriter(icsvWriter);
	}

	private final ICSVWriter writer;

	private CSVWriter(ICSVWriter writer) {
		this.writer = writer;
	}

	public void write(String... line) {
		writer.writeNext(line);
	}

		@Override
	public void close() throws IOException {
		writer.close();
	}
}
