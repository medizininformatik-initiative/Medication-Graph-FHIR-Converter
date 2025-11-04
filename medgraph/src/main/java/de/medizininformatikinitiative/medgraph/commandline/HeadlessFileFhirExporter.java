package de.medizininformatikinitiative.medgraph.commandline;

import de.medizininformatikinitiative.medgraph.DI;
import de.medizininformatikinitiative.medgraph.fhirexporter.FhirExportSources;
import de.medizininformatikinitiative.medgraph.fhirexporter.FileFhirExportSink;
import de.medizininformatikinitiative.medgraph.fhirexporter.FileFhirExportSinkFactory;
import org.apache.commons.cli.CommandLine;
import org.jetbrains.annotations.NotNull;
import org.neo4j.driver.Session;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;

/**
 * Command line utility that can be used to invoke the {@link FileFhirExportSink}.
 *
 * @author Markus Budeus
 */
public class HeadlessFileFhirExporter extends CommandLineUtility {

	private final FileFhirExportSinkFactory fhirExportFactory = DI.get(FileFhirExportSinkFactory.class);

	@Override
	@NotNull
	public ExitStatus invoke(CommandLine commandLine, List<String> args) {
		if (args.size() != 1) {
			printHelp();
			return ExitStatus.INCORRECT_USAGE;
		}

		Path exportPath;
		try {
			exportPath = Path.of(args.getFirst());
		} catch (InvalidPathException e) {
			return ExitStatus.invalidPath(e);
		}

		return withDatabaseConnection(con -> {
			try (Session session = con.createSession()) {
				FileFhirExportSink export = fhirExportFactory.prepareExport(exportPath);
				export.doExport(FhirExportSources.forNeo4jSession(session));
				return ExitStatus.SUCCESS;
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
