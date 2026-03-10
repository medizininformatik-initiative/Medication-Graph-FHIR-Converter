package de.medizininformatikinitiative.medgraph.rxnorm_matching;

import de.medizininformatikinitiative.medgraph.rxnorm_matching.RxNormProductMatcher2.MatchResult;
import de.medizininformatikinitiative.medgraph.rxnorm_matching.db.RxNormDatabaseImpl;
import de.medizininformatikinitiative.medgraph.rxnorm_matching.db.RxNormDatabasePool;
import de.medizininformatikinitiative.medgraph.rxnorm_matching.neo4j.model.Drug;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import static de.medizininformatikinitiative.medgraph.rxnorm_matching.RxNormMatcher.getRxNormDbCon;

/**
 * @author Markus Budeus
 */
public class MatchingExecutor {

	public static List<MatchResult> matchAll(Collection<Drug> drugs,
	                                         boolean silent) throws InterruptedException {
		return matchAll(drugs, silent, RxNormProductMatcher2::matchSCD);
	}


	public static List<RxNormProductMatcher2.ExtendedMatchResult> matchAllExtended(Collection<Drug> drugs,
	                                                               boolean silent) throws InterruptedException {
		return matchAll(drugs, silent, RxNormProductMatcher2::matchSCDWithDetailedInfo);
	}

	private static <T extends MatchResult> List<T> matchAll(Collection<Drug> drugs,
	                                                                  boolean silent,
	                                                                  BiFunction<RxNormProductMatcher2, Drug, T> matchingFunction) throws InterruptedException {
		DecimalFormat format = new DecimalFormat("0.0");
		int dbConnections = 8;
		int threads = 8;

		try (ForkJoinPool threadPool = new ForkJoinPool(threads)) {
			RxNormProductMatcher2 matcher2 = new RxNormProductMatcher2(
					new RxNormDatabasePool(
							() -> new RxNormDatabaseImpl(getRxNormDbCon()),
							dbConnections
					),
					new CustomDoseFormMapper(),
					false
			);

			int numDrugs = drugs.size();
			BlockingQueue<T> results = new LinkedBlockingQueue<>();

			if (!silent) System.out.println("Retrieved " + numDrugs + " drugs. Starting matching.");
			threadPool.submit(() ->
					drugs.stream()
					     .parallel()
					     .map(r -> matchingFunction.apply(matcher2, r))
					     .forEach(results::add)
			);

			List<T> allResults = new ArrayList<>(numDrugs);
			int successful = 0;
			for (int i = 1; i <= numDrugs; i++) {
				T result = (T) results.poll(5, TimeUnit.SECONDS);
				if (result != null && !result.getMatches().isEmpty()) successful++;
				allResults.add(result);
				if (!silent && (i % 100 == 0 || i == numDrugs)) {
					int progressBars = (50 * i / numDrugs);
					System.out.print("\r|" +
							"=".repeat(progressBars) +
							"-".repeat(50 - progressBars) +
							"| (" + format.format(100.0 * i / numDrugs) + " %, " +
							successful + "/" + i + " successful)");
				}
			}
			if (!silent) System.out.println();
//			results.forEach(System.out::println);
			List<T> successes = allResults.stream()
			                                        .filter(m -> !m.getMatches().isEmpty())
			                                        .toList();
			if (!silent) System.out.println(successes.size() + "/" + allResults.size() + " successful matches ("
					+ format.format(100.0 * successes.size() / allResults.size()) + "%)");

			return allResults;
		}
	}

}
