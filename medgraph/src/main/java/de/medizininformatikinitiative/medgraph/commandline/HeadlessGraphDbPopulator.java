package de.medizininformatikinitiative.medgraph.commandline;

import de.medizininformatikinitiative.medgraph.DI;
import de.medizininformatikinitiative.medgraph.common.logging.Level;
import de.medizininformatikinitiative.medgraph.common.logging.LogManager;
import de.medizininformatikinitiative.medgraph.common.logging.Logger;
import de.medizininformatikinitiative.medgraph.graphdbpopulator.GraphDbPopulation;
import de.medizininformatikinitiative.medgraph.graphdbpopulator.GraphDbPopulationFactory;
import org.apache.commons.cli.CommandLine;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;

/**
 * Command-Line tool which runs the Graph DB Populator in headless mode.
 *
 * @author Markus Budeus
 */
public class HeadlessGraphDbPopulator extends CommandLineUtility {

	private static final String INCLUDE_ARCHIVE = "include-archive";
	private static final Logger logger = LogManager.getLogger(HeadlessGraphDbPopulator.class);

	static final String CALL_ARG = "populate";

	static final String USAGE = CALL_ARG + " <path_to_mmi_pharmindex_files> <path_to_neo4j_import_dir> [path_to_amice_dataset] [" + INCLUDE_ARCHIVE + "]";

	private final GraphDbPopulationFactory factory = DI.get(GraphDbPopulationFactory.class);

	@Override
	public @NotNull ExitStatus invoke(CommandLine commandLine, List<String> args) {
		if (args.size() < 2 || args.size() > 4) {
			printHelp();
			return ExitStatus.INCORRECT_USAGE;
		}

		boolean includeArchive = false;
		String amicePathArg = null;

		if (args.size() > 2) {
			if (args.get(2).equals(INCLUDE_ARCHIVE)) {
				includeArchive = true;
				if (args.size() > 3) {
					amicePathArg = args.get(3);
				}
			} else {
				amicePathArg = args.get(2);
				if (args.size() > 3) {
					String include = args.get(3);
					if (include.equalsIgnoreCase(INCLUDE_ARCHIVE) || include.equalsIgnoreCase(
							"yes") || include.equalsIgnoreCase("true")) {
						includeArchive = true;
					} else {
						printHelp();
						return ExitStatus.INCORRECT_USAGE;
					}
				}
			}
		}

		Path mmiPharmindexPath;
		Path neo4jImportPath;
		Path amicePath = null;
		try {
			mmiPharmindexPath = Path.of(args.get(0));
			neo4jImportPath = Path.of(args.get(1));
			if (amicePathArg != null && !amicePathArg.isBlank()) amicePath = Path.of(amicePathArg);
		} catch (InvalidPathException e) {
			return ExitStatus.invalidPath(e);
		}

		final Path fixedAmicePath = amicePath;

		String format = "%31s %s";
		logger.log(Level.DEBUG,
				String.format(format, "Path to MMI Pharmindex files:", mmiPharmindexPath.toAbsolutePath()));
		logger.log(Level.DEBUG,
				String.format(format, "Path to Neo4j import directory:", neo4jImportPath.toAbsolutePath()));
		if (amicePath != null)
			logger.log(Level.DEBUG, String.format(format, "Path to AMIce data file:", amicePath.toAbsolutePath()));

		final boolean includeArchiveFinal = includeArchive;
		return withDatabaseConnection(connection -> {
			GraphDbPopulation population = factory.prepareDatabasePopulation(mmiPharmindexPath, neo4jImportPath,
					fixedAmicePath);
			try {
				logger.log(Level.INFO, "Starting headless database population.");
				logger.log(Level.INFO, "Archive loading is " + (includeArchiveFinal ? "enabled." : "disabled."));
				population.executeDatabasePopulation(connection, includeArchiveFinal);
				logger.log(Level.INFO, "Headless database population completed.");
				return ExitStatus.SUCCESS;
			} catch (AccessDeniedException e) {
				logger.log(Level.ERROR, "Access denied: " + e.getMessage());
				return ExitStatus.accessDenied(e);
			} catch (IOException e) {
				logger.log(Level.ERROR, "An I/O exception occurred.", e);
				return ExitStatus.ioException(e);
			}
		});
	}

	@Override
	public String getCallArgument() {
		return CALL_ARG;
	}

	public String getUsage() {
		return USAGE;
	}

	@Override
	protected void printHelp() {
		super.printHelp();
		System.out.println("To enable archive loading, but not using the AMIce file, " +
				"just pass an empty string as AMIce file path and '" +
				INCLUDE_ARCHIVE + "' as fourth argument.");
	}
}
