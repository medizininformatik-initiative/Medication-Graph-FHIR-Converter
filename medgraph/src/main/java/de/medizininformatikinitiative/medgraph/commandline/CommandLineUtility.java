package de.medizininformatikinitiative.medgraph.commandline;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;

import java.util.List;

/**
 * Class which provides some utility through the command line.
 *
 * @author Markus Budeus
 */
public abstract class CommandLineUtility {

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
