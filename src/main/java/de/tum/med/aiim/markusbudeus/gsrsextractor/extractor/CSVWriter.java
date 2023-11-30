package de.tum.med.aiim.markusbudeus.gsrsextractor.extractor;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class CSVWriter implements AutoCloseable {


	public static CSVWriter open(Path path) throws IOException {
		ICSVWriter writer = new CSVWriterBuilder(new FileWriter(path.toString()))
				.withSeparator(';')
				.build();
		return new CSVWriter(writer);
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
