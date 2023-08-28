package de.tum.markusbudeus;

import com.opencsv.exceptions.CsvException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CSVReader implements AutoCloseable {

	private final com.opencsv.CSVReader reader;

	private CSVReader(com.opencsv.CSVReader reader) {
		this.reader = reader;
	}

	public static CSVReader open(Path path) throws IOException {
		return new CSVReader(new com.opencsv.CSVReader(Files.newBufferedReader(path)));
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
