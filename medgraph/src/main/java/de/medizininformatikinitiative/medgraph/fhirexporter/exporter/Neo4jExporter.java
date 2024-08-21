package de.medizininformatikinitiative.medgraph.fhirexporter.exporter;

import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.util.stream.Stream;

/**
 * Class which exports data from the Neo4j Graph database.
 *
 * @param <T> the type of exported objects
 * @author Markus Budeus
 */
public abstract class Neo4jExporter<T> {

	protected final Session session;

	public Neo4jExporter(Session session) {
		this.session = session;
	}

	public abstract Stream<T> exportObjects();

}
