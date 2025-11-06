package de.medizininformatikinitiative.medgraph.commandline;

import de.medizininformatikinitiative.medgraph.DI;
import de.medizininformatikinitiative.medgraph.fhirexporter.FhirExportSources;
import de.medizininformatikinitiative.medgraph.fhirexporter.FhirServerExportSink;
import de.medizininformatikinitiative.medgraph.fhirexporter.FhirServerExportSinkFactory;
import org.apache.commons.cli.CommandLine;
import org.jetbrains.annotations.NotNull;
import org.neo4j.driver.Session;

import java.io.IOException;
import java.util.List;

import static de.medizininformatikinitiative.medgraph.commandline.CommandLineExecutor.OPTION_HTTP_BASIC_AUTH;
import static de.medizininformatikinitiative.medgraph.commandline.CommandLineExecutor.OPTION_TOKEN_AUTH;

/**
 * Command line utility that can be used to invoke the {@link FhirServerExportSink}.
 *
 * @author Markus Budeus
 */
public class HeadlessFhirServerExporter extends CommandLineUtility {

	private final FhirServerExportSinkFactory factory = DI.get(FhirServerExportSinkFactory.class);

	@Override
	@NotNull
	public ExitStatus invoke(CommandLine commandLine, List<String> args) {
		if (args.size() != 1) {
			printHelp();
			return ExitStatus.INCORRECT_USAGE;
		}

		String url = args.getFirst();
		String basicAuth = commandLine.getOptionValue(OPTION_HTTP_BASIC_AUTH.getOpt());
		String tokenAuth = commandLine.getOptionValue(OPTION_TOKEN_AUTH.getOpt());

		FhirServerExportSink sink;
		if (basicAuth != null) {

			if (tokenAuth != null) {
				System.err.println(
						"The -" + OPTION_HTTP_BASIC_AUTH.getOpt() + " and -" + OPTION_TOKEN_AUTH.getOpt() + " options cannot be used together.");
				return ExitStatus.INCORRECT_USAGE;
			}

			int splitIndex = basicAuth.indexOf(':');
			if (splitIndex == -1) {
				System.err.println(
						"No colon (:) found in HTTP basic auth information. A colon is used to separate username and password.");
				return ExitStatus.INCORRECT_USAGE;
			}
			String username = basicAuth.substring(0, splitIndex);
			char[] password = basicAuth.substring(splitIndex+1).toCharArray();
			sink = factory.prepareExportWithHttpBasicAuth(url, username, password);

		} else {
			if (tokenAuth != null) {
				sink = factory.prepareExportWithTokenAuth(url, tokenAuth);
			} else  {
				sink = factory.prepareExportWithoutAuth(url);
			}
		}

		return withDatabaseConnection(con -> {
			try (Session session = con.createSession()) {
				System.out.println("Attempting upload of FHIR resources to "+url+". This may take a few minutes.");
				sink.doExport(FhirExportSources.forNeo4jSession(session));
				System.out.println("Upload successful.");
				return ExitStatus.SUCCESS;
			} catch (IOException e) {
				return ExitStatus.ioException(e);
			}
		});
	}

	@Override
	public String getCallArgument() {
		return "export-fhir";
	}

	@Override
	public String getUsage() {
		return getCallArgument() + " <url> " +
				"[-" + OPTION_HTTP_BASIC_AUTH.getOpt() + " user:password] " +
				"[-" + OPTION_TOKEN_AUTH.getOpt() + " token]";
	}

}
