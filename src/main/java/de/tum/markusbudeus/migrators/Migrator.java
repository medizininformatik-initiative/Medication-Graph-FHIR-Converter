package de.tum.markusbudeus.migrators;

import de.tum.markusbudeus.CSVReader;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.io.IOException;
import java.nio.file.Path;

public abstract class Migrator implements AutoCloseable {

	protected final CSVReader reader;
	protected final Session session;

	public Migrator(Path directory, String filename, Session session) throws IOException {
		this(directory.resolve(filename), session);
	}

	public Migrator(Path path, Session session) throws IOException {
		this(CSVReader.open(path), session);
	}

	protected Migrator(CSVReader reader, Session session) {
		this.reader = reader;
		this.session = session;
	}

	public void migrate() throws IOException, InterruptedException {
		int lineNo = 0;
		boolean firstLine = true;
		String[] line;

		while ((line = reader.readNext()) != null) {
			if (lineNo % 10 == 0) {
				System.out.print("Executing " + getClass().getSimpleName() + ": " + lineNo + " entries complete\r");
				System.out.flush();
				throwIfInterrupted();
			}
			if (firstLine) {
				// Skip header line
				firstLine = false;
				continue;
			}
			lineNo++;
			try {
				migrateLine(line);
			} catch (RuntimeException e) {
				System.err.println("Failed to migrate line " + lineNo + "! Exception below.");
				e.printStackTrace();
			}
		}
		System.out.println(getClass().getSimpleName() + ": Migrated " + lineNo + " entries.");
	}

	private void throwIfInterrupted() throws InterruptedException {
		if (Thread.interrupted())
			throw new InterruptedException("Migration interrupted!");
	}

	public abstract void migrateLine(String[] line);

	/**
	 * Verifies the given result contains exactly one row. This consumes the result.
	 *
	 * @param result        the result to check
	 * @param errorNone     the error message to print if the result is empty
	 * @param errorMultiple the error message to print if the result contains multiple entries
	 */
	protected void assertSingleRow(Result result, String errorNone, String errorMultiple) {
		if (!result.hasNext()) {
			System.err.println(errorNone);
		}
		result.next();
		if (result.hasNext()) {
			System.err.println(errorMultiple);
		}
		result.consume();
	}

	@Override
	public void close() throws IOException {
		reader.close();
		session.close();
	}
}
