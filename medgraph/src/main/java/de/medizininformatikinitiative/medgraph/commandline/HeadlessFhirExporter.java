package de.medizininformatikinitiative.medgraph.commandline;

import de.medizininformatikinitiative.medgraph.DI;
import de.medizininformatikinitiative.medgraph.fhirexporter.FhirExport;
import de.medizininformatikinitiative.medgraph.fhirexporter.FhirExportFactory;
import org.apache.commons.cli.CommandLine;
import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.RxNormMatcherSetup;
import de.medizininformatikinitiative.medgraph.searchengine.db.Neo4jCypherDatabase;
import org.jetbrains.annotations.NotNull;
import org.neo4j.driver.Session;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;

/**
 * Command line utility that can be used to invoke the {@link FhirExport}.
 *
 * @author Markus Budeus
 */
public class HeadlessFhirExporter extends CommandLineUtility {

	private final FhirExportFactory fhirExportFactory = DI.get(FhirExportFactory.class);

	@Override
	@NotNull
	public ExitStatus invoke(CommandLine commandLine, List<String> args) {
		if (args.size() != 1) {
			printHelp();
			return ExitStatus.INCORRECT_USAGE;
		}

		Path exportPath;
		try {
			exportPath = Path.of(args.get(0));
		} catch (InvalidPathException e) {
			return ExitStatus.invalidPath(e);
		}

		return withDatabaseConnection(con -> {
			try (Session session = con.createSession()) {
				// Initialize RxNorm providers using the RxNav API (DoseFormMapper + RxNav-based resolvers/providers)
				Neo4jCypherDatabase db = new Neo4jCypherDatabase(session);
				RxNormMatcherSetup.initializeWithLocalProviders(db);
				RxNormMatcherSetup.createMatcher();

				// Initialize SCD Match CSV Writer
				Path csvPath = Path.of(System.getProperty("user.home"), "Desktop", "scd_matches.csv");
				de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.ScdMatchCsvWriter csvWriter = 
					de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.ScdMatchCsvWriter.getInstance();
				csvWriter.initialize(csvPath);

				try {
					FhirExport export = fhirExportFactory.prepareExport(exportPath);
					export.doExport(session);
					return ExitStatus.SUCCESS;
				} finally {
					// Close CSV writer at the end
					csvWriter.close();
				}
			} catch (AccessDeniedException e) {
				return ExitStatus.accessDenied(e);
			} catch (IOException e) {
				return ExitStatus.ioException(e);
			}
		});
	}

	@Override
	public String getCallArgument() {
		return "export";
	}

	@Override
	public String getUsage() {
		return getCallArgument() + " <path>";
	}
}
