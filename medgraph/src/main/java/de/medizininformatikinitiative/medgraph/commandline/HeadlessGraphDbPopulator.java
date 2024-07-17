package de.medizininformatikinitiative.medgraph.commandline;

import org.apache.commons.cli.CommandLine;

import java.util.List;

/**
 * Command-Line tool which runs the Graph DB Populator in headless mode.
 *
 * @author Markus Budeus
 */
public class HeadlessGraphDbPopulator extends CommandLineUtility {

	static final String CALL_ARG = "populate";

	static final String USAGE = CALL_ARG + " <path_to_mmi_pharmindex_files> <path_to_neo4j_import_dir> [path_to_amice_dataset]";

	@Override
	public ExitStatus invoke(CommandLine commandLine, List<String> args) {
		if (args.size() < 2 || args.size() > 3) {
			printHelp();
			return ExitStatus.INCORRECT_USAGE;
		}

		// TODO Ehh implement the utility?

		return null;
	}

	@Override
	public String getCallArgument() {
		return CALL_ARG;
	}

	public String getUsage() {
		return USAGE;
	};

}
