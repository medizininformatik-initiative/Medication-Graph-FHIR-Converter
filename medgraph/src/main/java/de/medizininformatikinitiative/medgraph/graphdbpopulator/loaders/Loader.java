package de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders;

import de.medizininformatikinitiative.medgraph.common.logging.Level;
import de.medizininformatikinitiative.medgraph.common.logging.LogManager;
import de.medizininformatikinitiative.medgraph.common.logging.Logger;
import org.neo4j.driver.Query;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

import static org.neo4j.driver.Values.parameters;

/**
 * Loaders are used to execute Cypher statements to load data into the database.
 *
 * @author Markus Budeus
 */
public abstract class Loader {

	private final Logger logger = LogManager.getLogger(getClass());
	private static final boolean DRY_RUN = false;

	private static final DateTimeFormatter cypherDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	protected final Session session;
	private long subtaskStartTime = -1L;

	private Consumer<String> onSubtaskStarted = null;
	private Runnable onSubtaskCompleted;

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
		logger.log(Level.INFO, "Running " + getClass().getSimpleName());
		System.out.print("Running " + getClass().getSimpleName() + "...");
		long time = System.currentTimeMillis();
		executeLoad();
		completeSubtask();
		logger.log(Level.INFO, getClass().getSimpleName() + " execution complete (" + (System.currentTimeMillis() - time) + "ms)");
	}

	/**
	 * Prints information to the console that a subtask has been started. Subtasks do not support nesting, starting
	 * a new subtask without completing the old one will complete the old one.
	 */
	protected void startSubtask(String subtask) {
		if (subtaskStartTime != -1) {
			completeSubtask();
		}
		logger.log(Level.DEBUG, getClass().getSimpleName() + " started a subtask: "+subtask);
		ifNotNull(onSubtaskStarted, listener -> listener.accept(subtask));
		subtaskStartTime = System.currentTimeMillis();
	}

	/**
	 * Prints information to the console that the last subtask is complete.
	 */
	protected void completeSubtask() {
		if (subtaskStartTime == -1) return;
		ifNotNull(onSubtaskCompleted, Runnable::run);
		logger.log(Level.DEBUG, getClass().getSimpleName() + " completed current subtask. ("+(System.currentTimeMillis() - subtaskStartTime)+"ms)");
		subtaskStartTime = -1;
	}

	protected abstract void executeLoad();

	/**
	 * Wraps the given statement into a "CALL { ... } IN TRANSACTIONS OF 5000 ROWS"
	 * @param statement the statement to wrap
	 * @return the statement wrapped into the CALL IN TRANSACTIONS structure
	 */
	public String withRowLimit(String statement) {
		return withRowLimit(statement, 5000);
	}

	/**
	 * Wraps the given statement into a "CALL { ... } IN TRANSACTIONS OF rowLimit ROWS"
	 * @param statement the statement to wrap
	 * @param rowLimit the row limit to use
	 * @return the statement wrapped into the CALL IN TRANSACTIONS structure
	 */
	public String withRowLimit(String statement, int rowLimit) {
		return "CALL { " + statement + "} IN TRANSACTIONS OF " + rowLimit + " ROWS";
	}

	/**
	 * Executes the given query and returns its result.
	 *
	 * @param statement the query to execute
	 * @return the result of the query or null if {@link #DRY_RUN} is active.
	 */
	public Result executeQuery(String statement, Object... params) {
		if (DRY_RUN) {
			for (int i = 0; i < params.length; i += 2) {
				statement = statement.replace("$"+params[i], params[i+1].toString());
			}
			System.out.println(statement);
			return null;
		} else {
			if (params.length == 0) {
				return session.run(new Query(statement));
			} else {
				return session.run(new Query(statement, parameters(params)));
			}
		}
	}

	/**
	 * Sets a callback to be invoked if this loader starts a subtask during execution.
	 * @param onSubtaskStarted the callback to invoke when a subtask starts
	 */
	public void setOnSubtaskStartedListener(Consumer<String> onSubtaskStarted) {
		this.onSubtaskStarted = onSubtaskStarted;
	}

	/**
	 * Sets a callback to be invoked if this loader completes a subtask during execution.
	 * @param onSubtaskCompleted the callback to invoke when a subtask completes
	 */
	public void setOnSubtaskCompletedListener(Runnable onSubtaskCompleted) {
		this.onSubtaskCompleted = onSubtaskCompleted;
	}

	private static <T> void ifNotNull(T object, Consumer<T> action) {
		if (object != null) {
			action.accept(object);
		}
	}
}
