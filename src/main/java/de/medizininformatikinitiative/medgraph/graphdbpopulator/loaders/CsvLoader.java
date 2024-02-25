package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import org.neo4j.driver.Session;

import java.io.IOException;
import java.nio.file.Path;

/**
 * CsvLoaders use the LOAD CSV function of Cypher to load bulk data.
 *
 * @author Markus Budeus
 */
public abstract class CsvLoader extends Loader {

	/**
	 * The path inside the Neo4j import directory where the MMI PharmIndex data resides.
	 */
	private static final Path MMI_PHARMINDEX_IMPORT_DIR = Path.of("mmi_pharmindex");

	/**
	 * The variable name assigned to the current CSV row for the LOAD CSV statement.
	 */
	protected static final String ROW_IDENTIFIER = "row";

	protected static final char DEFAULT_FIELD_TERMINATOR = ';';

	private final Path path;

	/**
	 * Loader referencing the CSV file with the given name inside the MMI PharmIndex dataset.
	 */
	public CsvLoader(String filename, Session session) throws IOException {
		this(MMI_PHARMINDEX_IMPORT_DIR.resolve(filename), session);
	}

	/**
	 * Loader referencing any file within the Neo4j import scope.
	 */
	public CsvLoader(Path path, Session session) throws IOException {
		super(session);
		this.path = path;
	}

	/**
	 * Returns the Path to the CSV file in a way which can be passed to Cypher's LOAD CSV function.
	 */
	public String getCypherFilePath() {
		return "file:///" + path.toString();
	}

	/**
	 * Creates the LOAD CSV statement and appends the given statement.
	 *
	 * @param statement the statement to execute for each row
	 * @return a full Cypher statement
	 */
	public String withLoadStatement(String statement) {
		return withLoadStatement(statement, DEFAULT_FIELD_TERMINATOR);
	}

	/**
	 * Creates the LOAD CSV statement and appends the given statement.
	 *
	 * @param statement       the statement to execute for each row
	 * @param fieldTerminator the field terminator to use when reading the CSV file
	 * @return a full Cypher statement
	 */
	public String withLoadStatement(String statement, char fieldTerminator) {
		return withLoadStatement(statement, fieldTerminator, true);
	}

	/**
	 * Creates the LOAD CSV statement and appends the given statement.
	 *
	 * @param statement       the statement to execute for each row
	 * @param fieldTerminator the field terminator to use when reading the CSV file
	 * @param withHeaders     if true, the CSV file is loaded with headers so you can access colums by name instead of
	 *                        index
	 * @return a full Cypher statement
	 */
	public String withLoadStatement(String statement, char fieldTerminator, boolean withHeaders) {
		return "LOAD CSV " + (withHeaders ? "WITH HEADERS " : "") + "FROM '" + getCypherFilePath() + "'"
				+ " AS " + ROW_IDENTIFIER
				+ " FIELDTERMINATOR '" + fieldTerminator + "' "
				+ statement;
	}

	/**
	 * Accesses a row entry by index. Only works if the CSV file was loaded without headers.
	 *
	 * @param index the index of the row to access
	 * @return a Cypher expression to access the corresponding index of the current row
	 */
	public String row(int index) {
		return ROW_IDENTIFIER + "[" + index + "]";
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

}
