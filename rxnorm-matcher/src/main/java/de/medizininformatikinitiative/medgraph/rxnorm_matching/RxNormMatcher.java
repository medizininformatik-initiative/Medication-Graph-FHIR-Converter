package de.medizininformatikinitiative.medgraph.rxnorm_matching;

import de.medizininformatikinitiative.medgraph.DI;
import de.medizininformatikinitiative.medgraph.common.db.ConnectionConfiguration;
import de.medizininformatikinitiative.medgraph.common.db.ConnectionPreferences;
import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnection;
import de.medizininformatikinitiative.medgraph.common.logging.LogManager;
import de.medizininformatikinitiative.medgraph.common.logging.Logger;
import de.medizininformatikinitiative.medgraph.rxnorm_matching.model.DetailedRxNormSCD;
import de.medizininformatikinitiative.medgraph.rxnorm_matching.neo4j.RxNormMatchingDrugLoader;
import de.medizininformatikinitiative.medgraph.rxnorm_matching.neo4j.model.Drug;
import org.neo4j.driver.Session;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static de.medizininformatikinitiative.medgraph.rxnorm_matching.RxNormProductMatcher2.EarlyMatchingFailure;
import static de.medizininformatikinitiative.medgraph.rxnorm_matching.RxNormProductMatcher2.MatchResult;

/**
 * @author Markus Budeus
 */
public class RxNormMatcher {

	private static Logger logger = LogManager.getLogger(RxNormMatcher.class);

	public static void main(String[] args) throws ExecutionException, InterruptedException {
		ConnectionPreferences preferences = DI.get(ConnectionPreferences.class);

		String uri = args.length > 0 ? args[0] : preferences.getConnectionUri();
		if (uri == null) uri = "bolt://localhost:7687";
		String user = args.length > 1 ? args[1] : preferences.getUser();
		if (user == null) user = "neo4j";
		char[] password = args.length > 2 ? args[2].toCharArray() : System.getenv().getOrDefault("NEO4J_PASSWORD", "")
		                                                                  .toCharArray();
		if (password.length == 0) {
			// Try locally saved connection config as fallback
			password = preferences.getPassword();
		}
		if (password == null) {
			System.err.println("WARNING: No Neo4j password provided!");
			System.err.println("Please set NEO4J_PASSWORD environment variable or pass as command line argument.");
			System.exit(1);
		}

		ConnectionConfiguration configuration = new ConnectionConfiguration(uri, user, password);

		try (DatabaseConnection connection = configuration.createConnection();
		     Session session = connection.createSession()) {

			RxNormMatchingDrugLoader drugLoader = new RxNormMatchingDrugLoader(session);

			long time = System.currentTimeMillis();

			List<Drug> drugs = drugLoader
					.loadDrugs(null)//List.of(1425163)) // Here we can put a product ID filter
//					.skip(25000)
//							.limit(10000)
					.toList();

			List<MatchResult> results = MatchingExecutor.matchAll(drugs, false);
			System.out.println("Done (" + (System.currentTimeMillis() - time) + "ms)");
			printGlobalStatistics(results);
			matchingToCsv(results);
			printResultsWithMultipleMatches(results);
		}

	}

	private static void printGlobalStatistics(List<MatchResult> results) {
		Map<EarlyMatchingFailure, Integer> earlyMatchingFailureCounts = new HashMap<>();
		int noCandidates = 0;
		int successes = 0;
		for (MatchResult result : results) {
			if (result.getEarlyMatchingFailure() != null) {
				earlyMatchingFailureCounts.compute(result.getEarlyMatchingFailure(),
						(k, v) -> v == null ? 1 : v + 1);
			} else if (result.getMatches().isEmpty()) {
				noCandidates++;
			} else {
				successes++;
			}
		}

		for (EarlyMatchingFailure failure: EarlyMatchingFailure.values()) {
			System.out.println(failure +": "+earlyMatchingFailureCounts.getOrDefault(failure, 0));
		}
		System.out.println("All candidates invalid: "+noCandidates);
		System.out.println("Successes: "+successes);
	}

	private static void matchingToCsv(List<MatchResult> results) {
		Path outPath = Path.of("medgraph/src/main/resources/rxnorm-mapping.csv");
		try (BufferedWriter writer = Files.newBufferedWriter(outPath)) {
			writer.write("MMI_DRUG_ID;SCD_RXCUI");
			for (MatchResult result: results) {
				for (DetailedRxNormSCD match: result.getMatches()) {
					writer.write(result.getDrug().productMmiId().toString());
					writer.write(';');
					writer.write(match.getRxcui());
					writer.write('\n');
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void printResultsWithMultipleMatches(List<MatchResult> results) {
		for (MatchResult result: results) {
			if (result.getMatches().size() > 1) {
				System.out.println(result);
			}
		}
	}

	public static Connection getRxNormDbCon() {
		try {
			return DriverManager.getConnection("jdbc:sqlite:data/rxnorm/rxnorm.db");
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
