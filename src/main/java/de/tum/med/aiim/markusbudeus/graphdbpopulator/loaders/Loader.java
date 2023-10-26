package de.tum.med.aiim.markusbudeus.graphdbpopulator.loaders;

import org.neo4j.driver.Query;
import org.neo4j.driver.Session;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.neo4j.driver.Values.parameters;

/**
 * Loaders are used to execute Cypher statements to load data into the database.
 */
public abstract class Loader {

	private static final boolean DRY_RUN = true;

	private static final DateTimeFormatter cypherDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	protected final Session session;

	public Loader(Session session) {
		this.session = session;
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
	 * If the given value is not null, it is returned in single quotes ('), otherwise the string null is returned.
	 */
	protected String quoteOrNull(String value) {
		return value == null ? "null" : "'" + value + "'";
	}

	protected String toCypherDate(LocalDate date) {
		return date == null ? "null" : "date('" + cypherDateFormatter.format(date) + "')";
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
	public void executeQuery(String statement, Object... params) {
		if (DRY_RUN) {
			for (int i = 0; i < params.length; i += 2) {
				statement = statement.replace("$"+params[i], params[i+1].toString());
			}
			System.out.println(statement);

		} else {
			if (params.length == 0) {
				session.run(new Query(statement));
			} else {
				session.run(new Query(statement, parameters(params)));
			}
		}
	}

}
