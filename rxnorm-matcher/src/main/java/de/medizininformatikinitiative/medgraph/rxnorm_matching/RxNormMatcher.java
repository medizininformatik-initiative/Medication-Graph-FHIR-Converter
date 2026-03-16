package de.medizininformatikinitiative.medgraph.rxnorm_matching;

import de.medizininformatikinitiative.medgraph.DI;
import de.medizininformatikinitiative.medgraph.common.db.ConnectionConfiguration;
import de.medizininformatikinitiative.medgraph.common.db.ConnectionPreferences;
import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnection;
import de.medizininformatikinitiative.medgraph.rxnorm_matching.RxNormProductMatcher2.ExtendedMatchResult;
import de.medizininformatikinitiative.medgraph.rxnorm_matching.RxNormProductMatcher2.ValidationResult;
import de.medizininformatikinitiative.medgraph.rxnorm_matching.model.DetailedRxNormSCD;
import de.medizininformatikinitiative.medgraph.rxnorm_matching.neo4j.RxNormMatchingDrugLoader;
import de.medizininformatikinitiative.medgraph.rxnorm_matching.neo4j.model.ActiveIngredient;
import de.medizininformatikinitiative.medgraph.rxnorm_matching.neo4j.model.Drug;
import org.neo4j.driver.Session;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

import static de.medizininformatikinitiative.medgraph.rxnorm_matching.RxNormProductMatcher2.EarlyMatchingFailure;
import static de.medizininformatikinitiative.medgraph.rxnorm_matching.RxNormProductMatcher2.MatchResult;

/**
 * @author Markus Budeus
 */
public class RxNormMatcher {

	public static void main(String[] args) throws InterruptedException {
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
					.loadDrugs(null)//List.of(543417)) // Here we can put a product ID filter
//					.skip(25000)
//					.limit(10000)
					.toList();

			List<MatchResult> results = MatchingExecutor.matchAll(drugs, false);
			System.out.println("Done (" + (System.currentTimeMillis() - time) + "ms)");
			printGlobalStatistics(results);
//			matchingToCsv(results);
//			printResultsWithMultipleMatches(results);
//			selectAndExportMatchesForReview(results, Path.of("review.csv"));
//			selectAndExportCandidateMismatchesForReview(results, new CustomDoseFormMapper(),
//					Path.of("mismatch_review.csv"));
		}

	}

	private static void printGlobalStatistics(List<? extends MatchResult> results) {
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

		for (EarlyMatchingFailure failure : EarlyMatchingFailure.values()) {
			System.out.println(failure + ": " + earlyMatchingFailureCounts.getOrDefault(failure, 0));
		}
		System.out.println("All candidates invalid: " + noCandidates);
		System.out.println("Successes: " + successes);
	}

	private static void matchingToCsv(List<? extends MatchResult> results) {
		Path outPath = Path.of("medgraph/src/main/resources/rxnorm_mapping.csv");
		try (BufferedWriter writer = Files.newBufferedWriter(outPath)) {
			writer.write("MMI_DRUG_ID;SCD_RXCUI\n");
			for (MatchResult result : results) {
				for (DetailedRxNormSCD match : result.getMatches()) {
					writer.write(result.getDrug().mmiId().toString());
					writer.write(';' );
					writer.write(match.getRxcui());
					writer.write('\n' );
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void printResultsWithMultipleMatches(List<MatchResult> results) {
		for (MatchResult result : results) {
			if (result.getMatches().size() > 1) {
				System.out.println(result);
			}
		}
	}

	private static void selectAndExportMatchesForReview(List<? extends MatchResult> results,
	                                                    Path outFile) {
		List<MatchResult> matches = new java.util.ArrayList<>(results.stream()
		                                                             .filter(r -> !r.getMatches().isEmpty())
		                                                             .toList());
		Random random = new Random(949684165194064L);
		Collections.shuffle(matches, random);

		try (BufferedWriter writer = Files.newBufferedWriter(outFile)) {
			writer.write("MMI_DRUG_ID;MMI_PRODUCT_ID;MMI_PRODUCT_NAME;MMI_DRUG_AMOUNT;MMI_INGREDIENTS;SCD_NAME\n");
			for (int i = 0; i < 200 && i < matches.size(); i++) {
				MatchResult result = matches.get(i);
				writer.write(result.getDrug().mmiId().toString());
				writer.write(';' );
				writer.write(result.getDrug().productMmiId().toString());
				writer.write(';' );
				writer.write(result.getDrug().productName());
				writer.write(';' );
				if (result.getDrug().amount() != null) {
					writer.write(result.getDrug().amount().toString());
					if (result.getDrug().getUnitName() != null) {
						writer.write(' ' );
						writer.write(result.getDrug().getUnitName());
					}
				}
				writer.write(';' );
				writer.write('"' );
				List<ActiveIngredient> ingredients = result.getDrug().activeIngredients();
				for (int j = 0; j < ingredients.size(); j++) {
					ActiveIngredient ingredient = ingredients.get(j);
					writer.write(ingredient.toStringWithCorrespondences());
					if (j < ingredients.size() - 1) {
						writer.write('\n' );
					}
				}
				writer.write('"' );
				writer.write(';' );
				writer.write('"' );
				List<DetailedRxNormSCD> resultMatches = result.getMatches();
				for (int j = 0; j < resultMatches.size(); j++) {
					DetailedRxNormSCD match = resultMatches.get(j);
					writer.write(match.getName());
					writer.write(" [");
					writer.write(match.getRxcui());
					writer.write(']' );
					if (j < resultMatches.size()) {
						writer.write('\n' );
					}
				}
				writer.write('"' );
				writer.write('\n' );
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	private static void selectAndExportCandidateMismatchesForReview(List<? extends ExtendedMatchResult> results,
	                                                                DoseFormMapper doseFormMapper,
	                                                                Path outFile) {
		List<ExtendedMatchResult> matches = new java.util.ArrayList<>(results.stream()
		                                                                     .filter(r -> r.getEarlyMatchingFailure() == null)
		                                                                     .filter(r -> r.getMatches().isEmpty())
		                                                                     .toList());
		Random random = new Random(1817406984601L);
		Collections.shuffle(matches, random);

		try (BufferedWriter writer = Files.newBufferedWriter(outFile)) {
			writer.write(
					"MMI_DRUG_ID;MMI_PRODUCT_ID;MMI_DRUG_AMOUNT;MMI_PRODUCT_NAME;MMI_INGREDIENTS;DERIVED_RXNORM_DOSE_FORM;SCD_CANDIDATES\n");
			for (int i = 0; i < 100 && i < matches.size(); i++) {
				ExtendedMatchResult result = matches.get(i);
				Drug drug = result.getDrug();
				writer.write(drug.mmiId().toString());
				writer.write(';' );
				writer.write(drug.productMmiId().toString());
				writer.write(';' );
				if (drug.amount() != null) {
					writer.write(drug.amount().toString());
					if (drug.getUnitName() != null) {
						writer.write(' ' );
						writer.write(drug.getUnitName());
					}
				}
				writer.write(';' );
				writer.write(drug.productName());
				writer.write(';' );
				writer.write('"' );
				List<ActiveIngredient> ingredients = drug.activeIngredients();
				for (int j = 0; j < ingredients.size(); j++) {
					ActiveIngredient ingredient = ingredients.get(j);
					writer.write(ingredient.toStringWithCorrespondences());
					if (j < ingredients.size() - 1) {
						writer.write('\n' );
					}
				}
				writer.write('"' );
				writer.write(';' );
				String rxNormDf = doseFormMapper.getRxNormDoseForm(drug.edqmDoseForm().getName());
				writer.write(rxNormDf == null ? "" : rxNormDf);
				writer.write(';' );
				writer.write('"' );
				Set<Map.Entry<DetailedRxNormSCD, ValidationResult>> candidateResultSet = result.getCandidateResults()
				                                                                               .entrySet();
				List<Map.Entry<DetailedRxNormSCD, ValidationResult>> candidateResults = new ArrayList<>(
						candidateResultSet);
				candidateResults.sort(new CandidateResultComparator());
				for (int j = 0; j < candidateResults.size(); j++) {
					DetailedRxNormSCD match = candidateResults.get(j).getKey();
					ValidationResult candidateResult = candidateResults.get(j).getValue();
					writer.write("[");
					writer.write(candidateResult.toString());
					writer.write("] ");
					writer.write(match.getName());
					writer.write(" [");
					writer.write(match.getRxcui());
					writer.write(']' );
					if (j < candidateResults.size()) {
						writer.write('\n' );
					}
				}
				writer.write('"' );
				writer.write('\n' );
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static Connection getRxNormDbCon() {
		try {
			return DriverManager.getConnection("jdbc:sqlite:data/rxnorm/rxnorm.db");
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private static class CandidateResultComparator implements Comparator<Map.Entry<DetailedRxNormSCD, ValidationResult>> {

		@Override
		public int compare(Map.Entry<DetailedRxNormSCD, ValidationResult> o1,
		                   Map.Entry<DetailedRxNormSCD, ValidationResult> o2) {
			int result = o1.getValue().compareTo(o2.getValue());
			if (result == 0) {
				return o1.getKey().getName().compareTo(o2.getKey().getName());
			}
			return result;
		}
	}

}
