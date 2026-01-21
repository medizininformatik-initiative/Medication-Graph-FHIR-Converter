package de.medizininformatikinitiative.medgraph.commandline;

import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.RxNormMatcherSetup;
import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.RxNormProductMatcher;
import de.medizininformatikinitiative.medgraph.fhirexporter.neo4j.TopSubstancesSCDCoverageAnalyzer;
import de.medizininformatikinitiative.medgraph.searchengine.db.Neo4jCypherDatabase;
import org.apache.commons.cli.CommandLine;
import org.jetbrains.annotations.NotNull;
import org.neo4j.driver.Session;

import java.util.List;

/**
 * Command-Line-Utility zur Analyse der SCD-Abdeckung der Top 20 Wirkstoffe.
 * 
 * @author Lucy Strüfing
 */
public class TopSubstancesAnalyzerCLI extends CommandLineUtility {

	@Override
	@NotNull
	public ExitStatus invoke(CommandLine commandLine, List<String> args) {
		// Optional JSON output file (first argument)
		String jsonOutputFile = args.isEmpty() ? null : args.get(0);

		return withDatabaseConnection(con -> {
			try (Session session = con.createSession()) {
				Neo4jCypherDatabase db = new Neo4jCypherDatabase(session);
				RxNormMatcherSetup.initializeWithLocalProviders(db);
				RxNormProductMatcher matcher = RxNormMatcherSetup.createMatcher();

				TopSubstancesSCDCoverageAnalyzer analyzer = new TopSubstancesSCDCoverageAnalyzer(session, matcher);
				analyzer.analyze(jsonOutputFile);
				
				return ExitStatus.SUCCESS;
			} catch (Exception e) {
				System.err.println("Error during analysis: " + e.getMessage());
				e.printStackTrace();
				return ExitStatus.internalError(e);
			}
		});
	}

	@Override
	public String getCallArgument() {
		return "analyze-top-substances";
	}

	@Override
	public String getUsage() {
		return getCallArgument();
	}
}

