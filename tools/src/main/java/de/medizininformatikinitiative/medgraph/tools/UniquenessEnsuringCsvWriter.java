package de.medizininformatikinitiative.medgraph.tools;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Wrapper around another {@link CSVWriter} which ensures that the same line cannot be written twice.
 *
 * @author Markus Budeus
 */
public class UniquenessEnsuringCsvWriter implements CSVWriter {

	private final CSVWriter writer;

	private final Set<List<String>> writtenLines = new HashSet<>();

	public UniquenessEnsuringCsvWriter(CSVWriter writer) {
		this.writer = writer;
	}

	@Override
	public void write(String... line) {
		List<String> list = List.of(line);
		if (writtenLines.add(list)) {
			writer.write(line);
		}
	}

	@Override
	public void close() throws IOException {
		writer.close();
	}
}
