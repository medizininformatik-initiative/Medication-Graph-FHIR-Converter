package de.medizininformatikinitiative.medgraph.commandline;

import de.medizininformatikinitiative.medgraph.common.db.ConnectionConfiguration;
import org.apache.commons.cli.*;
import org.jetbrains.annotations.Contract;

import java.util.Scanner;

/**
 * This class is responsible for parsing command-line arguments and orchestrating a headless run of the application, if
 * necessary.
 *
 * @author Markus Budeus
 */
public class CommandLineExecutor {

	// TODO Y'know. Tests.

	private static final String UTILITY_NAME = "medgraph";

	/**
	 * Evaluates the given command line arguments. If necessary, this orchestrates a full headless run of the
	 * application. In case the command line indicates a specific task to be performed by the application, this task is
	 * executed and the application exits from within this function.
	 * <p>
	 * If this function returns normally, this means the UI is supposed to be launched.
	 *
	 * @param args the command line arguments to evaluate
	 */
	public void evaluateAndExecuteCommandLineArguments(String[] args) {
		if (args == null || args.length == 0) return;

		Options options = new Options();
		for (Option option : constructOptions()) {
			options.addOption(option);
		}

		CommandLineParser parser = new DefaultParser();
		CommandLine commandLine = null;

		try {
			commandLine = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			new HelpFormatter().printHelp(UTILITY_NAME, options);
			System.exit(ExitStatus.COMMAND_LINE_PARSING_UNSUCCESSFUL.code);
		}

		if (commandLine.hasOption("help")) {
			new HelpFormatter().printHelp(UTILITY_NAME, options);
			System.exit(ExitStatus.SUCCESS.code);
		}

		executeCommandLine(commandLine);
	}

	/**
	 * Adds all different command line options used by this application to the given {@link Options} object.
	 */
	private Option[] constructOptions() {
		return new Option[]{
				new Option("h", "help", false, "Prints this usage dialog."),
				new Option("r", "database-uri", true, "The URI of the Neo4j service to connect to."),
				new Option("u", "database-user", true, "The user to authenticate at the Neo4j service with."),
				new Option("p", "database-password", true, "The password to authenticate at the Neo4j service with."),
				new Option("pi", "database-passin", false, "Reads the Neo4j database password to use from system-in.")
		};
	}

	/**
	 * Executes the given command line. In case the command line indicates a specific task to be performed by the
	 * application, this task is executed and the application exits from within this function.
	 * <p>
	 * If this function returns normally, this means the UI is supposed to be launched.
	 *
	 * @param commandLine the command line to execute
	 */
	private void executeCommandLine(CommandLine commandLine) {
		applyDbConnectionOptions(commandLine);

	}

	/**
	 * Sets the {@link ConnectionConfiguration} default configuration based on the connection information passed via the
	 * command line.
	 */
	private void applyDbConnectionOptions(CommandLine commandLine) {
		String dbUri = commandLine.getOptionValue("database-uri");
		String dbUser = commandLine.getOptionValue("database-user");
		String dbPass = commandLine.getOptionValue("database-password");
		if (commandLine.hasOption("database-passin")) {
			dbPass = readSingleLineFromStdIn();
		}

		if (dbUri != null || dbUser != null || dbPass != null) {
			if (dbUri == null || dbUser == null || dbPass == null) {
				exit(ExitStatus.INCOMPLETE_CONNECTION_DATA);
			}
			ConnectionConfiguration.setDefault(new ConnectionConfiguration(dbUri, dbUser, dbPass.toCharArray()));
		}
	}

	private String readSingleLineFromStdIn() {
		// This may look like a resource leak, but closing the Scanner would close System.in, which we do not want!
		Scanner scanner = new Scanner(System.in);
		return scanner.nextLine();
	}

	/**
	 * Dispatches the given exit status and exits the application.
	 */
	@Contract("_ -> fail")
	private void exit(ExitStatus exitStatus) {
		System.out.println(exitStatus.message);
		System.exit(exitStatus.code);
	}

}
