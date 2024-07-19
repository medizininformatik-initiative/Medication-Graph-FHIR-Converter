package de.medizininformatikinitiative.medgraph.commandline;

import de.medizininformatikinitiative.medgraph.DI;
import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnection;
import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnectionException;
import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnectionService;
import de.medizininformatikinitiative.medgraph.common.logging.Level;
import de.medizininformatikinitiative.medgraph.common.logging.LogManager;
import de.medizininformatikinitiative.medgraph.common.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;

import java.util.List;
import java.util.function.Function;

/**
 * Class which provides some utility through the command line.
 *
 * @author Markus Budeus
 */
public abstract class CommandLineUtility {

	private static final Logger logger = LogManager.getLogger(CommandLineUtility.class);
	private final DatabaseConnectionService connectionService = DI.get(DatabaseConnectionService.class);

	/**
	 * Invokes this command line utility. The given command line parameter is the parsed command line as passed to the
	 * master program, including options. The separately passed arguments list are the positional arguments excluding
	 * the one used to select this utility. For example, if this utility has the call argument "parse" and the program
	 * were invoked with the arguments:<p>
	 * <code>parse a.txt b.txt -c utf-8</code><p>
	 * Then the arguments would be "a.txt" and "b.txt." (Assuming -c is a valid option with an argument.)
	 *
	 * @param commandLine the full command line which was invoked on the master program
	 * @param args        the arguments provided to this utility, not including the argument used to invoke this
	 *                    utility
	 * @return the exit status to exit the program with
	 */
	public abstract ExitStatus invoke(CommandLine commandLine, List<String> args);

	/**
	 * Ensures a valid, functioning database connection is configured as the default, otherwise returns a corresponding
	 * exit code. If there is a functioning database configuration, the given action is invoked and its exit status
	 * returned.
	 *
	 * @param action the action to invoke in case the database connection is functional
	 * @return an exit status indicating what is wrong with the database connection or the exit status returned by the
	 * given action if the database connection is functional
	 */
	protected ExitStatus withDatabaseConnection(Function<DatabaseConnection, ExitStatus> action) {
		try (DatabaseConnection connection = connectionService.createConnection(true)){
			return action.apply(connection);
		} catch (DatabaseConnectionException e) {
			logger.log(Level.ERROR, "Failed to connect to database!", e);
			return switch (e.getConnectionResult()) {
				case INVALID_CONNECTION_STRING -> ExitStatus.INVALID_DB_CONNECTION_STRING;
				case SERVICE_UNAVAILABLE -> ExitStatus.NEO4J_SERVICE_UNAVAILABLE;
				case AUTHENTICATION_FAILED -> ExitStatus.NEO4J_AUTHENTICATION_FAILED;
				case INTERNAL_ERROR -> ExitStatus.internalError(e.getCause());
			};
		}
	}

	/**
	 * Returns the call argument used to invoke this utility.
	 */
	public abstract String getCallArgument();

	/**
	 * Returns a human-readable description of this utility's invocation syntax. (For example "&lt;input_path&gt;
	 * &lt;output_path&gt; [options]").
	 */
	public abstract String getUsage();

	/**
	 * Prints the usage dialog of this utility.
	 */
	protected void printHelp() {
		new HelpFormatter().printHelp(getUsage(), CommandLineExecutor.OPTIONS);
	}

}
