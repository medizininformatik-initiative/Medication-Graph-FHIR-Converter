package de.tum.markusbudeus;

import org.neo4j.driver.Session;

import java.io.IOException;

public abstract class Migrator implements AutoCloseable {

	protected final CSVReader reader;
	protected final Session session;

	protected Migrator(CSVReader reader, Session session) {
		this.reader = reader;
		this.session = session;
	}

	public abstract void migrate() throws IOException;

	@Override
	public void close() throws IOException {
		reader.close();
		session.close();
	}
}
