package de.medizininformatikinitiative.medgraph.commandline;

import de.medizininformatikinitiative.medgraph.common.db.ConnectionConfiguration;
import org.apache.commons.cli.*;

import java.io.InputStream;
import java.util.OptionalInt;
import java.util.Scanner;

/**
 * This class is responsible for parsing command-line arguments and orchestrating a headless run of the application, if
 * necessary.
 *
 * @author Markus Budeus
 */
public class CommandLineExecutor {

	static final Options OPTIONS = new Options();
	static final Option OPTION_HELP = new Option("h", "help", false, "Prints this usage dialog.");
	static final Option OPTION_DB_URI = new Option("r", "database-uri", true,
			"The URI of the Neo4j service to connect to.");
	static final Option OPTION_DB_USER = new Option("u", "database-user", true,
			"The user to authenticate at the Neo4j service with.");
	static final Option OPTION_DB_PASSWORD = new Option("p", "database-password", true,
			"The password to authenticate at the Neo4j service with.");
	static final Option OPTION_DB_PASSIN = new Option("pi", "database-passin", false,
			"Reads the Neo4j database password to use from system-in.");

	static {
		OPTIONS.addOption(OPTION_HELP);
		OPTIONS.addOption(OPTION_DB_URI);
		OPTIONS.addOption(OPTION_DB_USER);
		OPTIONS.addOption(OPTION_DB_PASSWORD);
		OPTIONS.addOption(OPTION_DB_PASSIN);
	}

	private static final String UTILITY_NAME = "medgraph";

	/**
	 * The input stream from which to read the password if required.
	 */
	private final InputStream inputStream;

	public CommandLineExecutor() {
		this(System.in);
	}

	public CommandLineExecutor(InputStream inputStream) {
		this.inputStream = inputStream;
	}


	/**
	 * Evaluates the given command line arguments. If necessary, this orchestrates a full headless run of the
	 * application. In case the command line indicates a specific task to be performed by the application, this task is
	 * executed and the corresponding exit code returned.
	 * <p>
	 * If this function returns null, this means the UI is supposed to be launched.
	 *
	 * @param args the command line arguments to evaluate
	 * @return an {@link OptionalInt} which indicates the process exit code to send or an empty optional indicating the
	 * process shall continue by launching the GUI
	 */
	public OptionalInt evaluateAndExecuteCommandLineArguments(String[] args) {
		if (args == null || args.length == 0) return OptionalInt.empty();

		CommandLineParser parser = new DefaultParser();
		CommandLine commandLine;

		try {
			commandLine = parser.parse(OPTIONS, args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			new HelpFormatter().printHelp(UTILITY_NAME, OPTIONS);
			return exit(ExitStatus.COMMAND_LINE_PARSING_UNSUCCESSFUL);
		}

		if (commandLine.hasOption("help")) {
			new HelpFormatter().printHelp(UTILITY_NAME, OPTIONS);
			return exit(ExitStatus.SUCCESS);
		}

		return executeCommandLine(commandLine);
	}

	/**
	 * Executes the given command line. If necessary, this orchestrates a full headless run of the application. In case
	 * the command line indicates a specific task to be performed by the application, this task is executed and the
	 * corresponding exit code returned.
	 *
	 * @param commandLine the command line to execute
	 * @return an {@link OptionalInt} indicating the process exit code to exit with or an empty optional if the process
	 * shall continue
	 */
	private OptionalInt executeCommandLine(CommandLine commandLine) {
		return applyDbConnectionOptions(commandLine);

	}

	/**
	 * Sets the {@link ConnectionConfiguration} default configuration based on the connection information passed via the
	 * command line.
	 */
	private OptionalInt applyDbConnectionOptions(CommandLine commandLine) {
		String dbUri = commandLine.getOptionValue(OPTION_DB_URI.getOpt());
		String dbUser = commandLine.getOptionValue(OPTION_DB_USER.getOpt());
		String dbPass = commandLine.getOptionValue(OPTION_DB_PASSWORD.getOpt());
		if (commandLine.hasOption(OPTION_DB_PASSIN.getOpt())) {
			dbPass = readSingleLineFromStdIn();
		}

		if (dbUri != null || dbUser != null || dbPass != null) {
			if (dbUri == null || dbUser == null || dbPass == null) {
				return exit(ExitStatus.INCOMPLETE_CONNECTION_DATA);
			}
			ConnectionConfiguration.setDefault(new ConnectionConfiguration(dbUri, dbUser, dbPass.toCharArray()));
		}
		return OptionalInt.empty();
	}

	private String readSingleLineFromStdIn() {
		// This may look like a resource leak, but closing the Scanner would close System.in, which we do not want!
		Scanner scanner = new Scanner(inputStream);
		return scanner.nextLine();
	}

	/**
	 * Prints the status message associated with the given status (unless it's null) and then returns the status code
	 * wrapped in an {@link OptionalInt}.
	 */
	private OptionalInt exit(ExitStatus exitStatus) {
		if (exitStatus.message != null)
			System.out.println(exitStatus.message);
		return OptionalInt.of(exitStatus.code);
	}

}
