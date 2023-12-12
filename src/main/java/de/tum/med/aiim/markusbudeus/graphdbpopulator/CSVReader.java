package de.tum.med.aiim.markusbudeus.graphdbpopulator;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CSVReader implements AutoCloseable {

	private final com.opencsv.CSVReader reader;

	private CSVReader(com.opencsv.CSVReader reader) {
		this.reader = reader;
	}

	public static CSVReader open(Path path) throws IOException {
		return open(Files.newBufferedReader(path));
	}

	public static CSVReader open(InputStream inputStream) throws IOException {
		return open(new InputStreamReader(inputStream));
	}

	public static CSVReader open(Reader reader) {
		CSVParser parser = new CSVParserBuilder()
				.withSeparator(';')
				.build();

		return new CSVReader(
				new CSVReaderBuilder(reader)
						.withCSVParser(parser)
						.build()
		);
	}

	public List<String[]> readAll() throws IOException {
		return wrapCsvException(reader::readAll);
	}

	/**
	 * Reads the next line or returns null if the end of the file has been reached.
	 */
	public String[] readNext() throws IOException {
		return wrapCsvException(reader::readNext);
	}

	@Override
	public void close() throws IOException {
		reader.close();
	}

	private <T> T wrapCsvException(CsvSupplier<T> supplier) throws IOException {
		try {
			return supplier.get();
		} catch (CsvException e) {
			throw new IOException(e);
		}
	}

	@FunctionalInterface
	private interface CsvSupplier<T> {
		T get() throws IOException, CsvException;
	}

}
