package de.tum.med.aiim.markusbudeus.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Loaders use the LOAD CSV function of Cypher to load bulk data.
 */
public abstract class Loader {

	protected final Session session;
	private final Path path;

	public Loader(Path directory, String filename, Session session) throws IOException {
		this(directory.resolve(filename), session);
	}

	public Loader(Path path, Session session) throws IOException {
		this.path = path.toAbsolutePath();
		this.session = session;
	}

	/**
	 * Returns the Path to the CSV file in a way which can be passed to Cypher's LOAD CSV function.
	 */
	public String getCypherFilePath() {
		return "file://"+path.toString();
	}

	/**
	 * Creates a Cypher expression which returns null if the given expression returns a blank string and otherwise
	 * returns the string.
	 * @param expression the expression to evaluate
	 * @return the value of the expression or null if the value of the expression is a blank string
	 */
	public String nullIfBlank(String expression) {
		return "CASE trim("+expression+") WHEN \"\" THEN null ELSE "+expression+" END";
	}

	public String constructLoadStatement(String rowIdentifier, String statement) {
		return "LOAD CSV WITH HEADERS FROM "+getCypherFilePath()+" AS "+ rowIdentifier + " " + statement;
	}

	public void execute() {
		executeLoad();
	}

	protected abstract void executeLoad();

}
