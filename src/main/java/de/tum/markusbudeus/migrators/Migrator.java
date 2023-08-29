package de.tum.markusbudeus.migrators;

import de.tum.markusbudeus.CSVReader;
import org.neo4j.driver.Session;

import java.io.IOException;
import java.nio.file.Path;

public abstract class Migrator implements AutoCloseable {

	protected final CSVReader reader;
	protected final Session session;

	protected Migrator(Path directory, String filename, Session session) throws IOException {
		this(CSVReader.open(directory.resolve(filename)), session);
	}

	protected Migrator(CSVReader reader, Session session) {
		this.reader = reader;
		this.session = session;
	}

	public void migrate() throws IOException {
		int lineNo = 0;
		String[] line;
		while ((line = reader.readNext()) != null) {
			lineNo++;
			try {
				migrateLine(line);
			} catch (RuntimeException e) {
				System.err.println("Failed to migrate line " + lineNo + "! Exception below.");
				e.printStackTrace();
			}
		}
		System.out.println(getClass().getSimpleName() + ": Migrated " + lineNo + " lines.");
	}

	public abstract void migrateLine(String[] line);

	@Override
	public void close() throws IOException {
		reader.close();
		session.close();
	}
}
