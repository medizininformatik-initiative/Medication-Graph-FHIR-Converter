package de.tum.med.aiim.markusbudeus.graphdbpopulator.loaders;

import org.neo4j.driver.Query;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.io.IOException;
import java.nio.file.Path;

import static org.neo4j.driver.Values.parameters;

/**
 * Loaders use the LOAD CSV function of Cypher to load bulk data.
 */
public abstract class Loader {

	/**
	 * The path inside the Neo4j import directory where the MMI PharmIndex data resides.
	 */
	private static final Path MMI_PHARMINDEX_IMPORT_DIR = Path.of("mmi_pharmindex");

	/**
	 * The variable name assigned to the current CSV row for the LOAD CSV statement.
	 */
	protected static final String ROW_IDENTIFIER = "row";

	protected final Session session;
	private final Path path;

	/**
	 * Loader referencing the CSV file with the given name inside the MMI PharmIndex dataset.
	 */
	public Loader(String filename, Session session) throws IOException {
		this(MMI_PHARMINDEX_IMPORT_DIR.resolve(filename), session);
	}

	/**
	 * Loader referencing any file within the Neo4j import scope.
	 */
	public Loader(Path path, Session session) throws IOException {
		this.path = path;
		this.session = session;
	}

	/**
	 * Returns the Path to the CSV file in a way which can be passed to Cypher's LOAD CSV function.
	 */
	public String getCypherFilePath() {
		return "file:///" + path.toString();
	}

	/**
	 * Creates a Cypher expression which returns null if the given expression returns a blank string and otherwise
	 * returns the string.
	 *
	 * @param expression the expression to evaluate
	 * @return the value of the expression or null if the value of the expression is a blank string
	 */
	public String nullIfBlank(String expression) {
		return "CASE trim(" + expression + ") WHEN \"\" THEN null ELSE " + expression + " END";
	}

	/**
	 * Creates the LOAD CSV statement and appends the given statement.
	 *
	 * @param statement the statement to execute for each row
	 * @return a full Cypher statement
	 */
	public String withLoadStatement(String statement) {
		return withLoadStatement(statement, ';');
	}

	/**
	 * Creates the LOAD CSV statement and appends the given statement.
	 *
	 * @param statement the statement to execute for each row
	 * @param fieldTerminator the field terminator to use when reading the CSV file
	 * @return a full Cypher statement
	 */
	public String withLoadStatement(String statement, char fieldTerminator) {
		return "LOAD CSV WITH HEADERS FROM '" + getCypherFilePath() + "'"
				+ " AS " + ROW_IDENTIFIER
				+ " FIELDTERMINATOR '"+fieldTerminator+"' "
				+ statement;
	}

	/**
	 * Creates a Cypher expression which retrieves the value of the column with the given header name in the current
	 * row. I.e., if you pass the header name "City", this will be converted to row.City.
	 *
	 * @param headerName the header name
	 * @return a Cypher expression to access the corresponding field of the current row
	 */
	public String row(String headerName) {
		return ROW_IDENTIFIER + "." + headerName;
	}

	/**
	 * Creates a Cypher expression which retrieves the value of the column with the given header name in the current row
	 * and parses it to an integer. I.e., if you pass the header name "Amount", this will be converted to
	 * "toInteger(row.Amount)".
	 *
	 * @param headerName the header name
	 * @return a Cypher expression to access the corresponding field of the current row as integer
	 */
	public String intRow(String headerName) {
		return "toInteger(" + row(headerName) + ")";
	}

	/**
	 * Creates a Cypher expression which retrieves the value of the column with the given header name in the current row
	 * and parses it to a float. I.e., if you pass the header name "Amount", this will be converted to
	 * "toFloat(row.Amount)".
	 *
	 * @param headerName the header name
	 * @return a Cypher expression to access the corresponding field of the current row as integer
	 */
	public String floatRow(String headerName) {
		return "toFloat(" + row(headerName) + ")";
	}

	public void execute() {
		System.out.print("Running " + getClass().getSimpleName() + "...");
		long time = System.currentTimeMillis();
		executeLoad();
		System.out.println("done (" + (System.currentTimeMillis() - time) + "ms)");
	}

	protected abstract void executeLoad();

	/**
	 * Executes the given query and returns its result.
	 *
	 * @param statement the query to execute
	 * @return the query's result
	 */
	public Result executeQuery(String statement, Object... params) {
		if (params.length == 0) {
			return session.run(new Query(statement));
		} else {
			return session.run(new Query(statement, parameters(params)));
		}
	}

}
