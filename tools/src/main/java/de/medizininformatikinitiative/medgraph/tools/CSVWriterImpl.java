package de.medizininformatikinitiative.medgraph.tools;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;

import java.io.IOException;
import java.io.Writer;

/**
 * Wrapper around whichever CSV writing library is used.
 *
 * @author Markus Budeus
 */
class CSVWriterImpl implements CSVWriter {

	static CSVWriterImpl open(Writer writer) throws IOException {
		ICSVWriter icsvWriter = new CSVWriterBuilder(writer)
				.withSeparator(';')
				.build();
		return new CSVWriterImpl(icsvWriter);
	}

	private final ICSVWriter writer;

	protected CSVWriterImpl(ICSVWriter writer) {
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
