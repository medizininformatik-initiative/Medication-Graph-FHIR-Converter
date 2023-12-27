package de.tum.med.aiim.markusbudeus.matcher;

import de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseConnection;
import de.tum.med.aiim.markusbudeus.matcher.algorithms.MatchingAlgorithm;
import de.tum.med.aiim.markusbudeus.matcher.algorithms.SampleAlgorithm;
import de.tum.med.aiim.markusbudeus.matcher.provider.IdentifierTarget;
import de.tum.med.aiim.markusbudeus.matcher.sample.synthetic.HouselistMatcher;
import de.tum.med.aiim.markusbudeus.matcher.sample.synthetic.SyntheticHouselistEntry;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class Main {

	public static void main(String[] args) throws IOException {

		List<SyntheticHouselistEntry> entries = HouselistMatcher.loadHouselist();


		DatabaseConnection.runSession(session -> {
			MatchingAlgorithm algorithm = new SampleAlgorithm(session);

			processStreamAndPrintResults(entries.stream()
			                                    .parallel()
			                                    .map(e -> new MatchingResult(e, algorithm.match(e))));
		});

	}

	private static void processStreamAndPrintResults(Stream<MatchingResult> stream) {
		AtomicInteger total = new AtomicInteger();
		AtomicInteger unique = new AtomicInteger();
		AtomicInteger ambiguous = new AtomicInteger();
		AtomicInteger unmatched = new AtomicInteger();
		stream.forEach(result -> {
			total.getAndIncrement();
			List<IdentifierTarget> bestMatches = result.result;

			if (bestMatches.size() == 1) {
				unique.getAndIncrement();
			} else if (bestMatches.isEmpty()) {
				unmatched.getAndIncrement();
			} else {
				ambiguous.getAndIncrement();
			}

			System.out.print(result.searchTerm.name + " -> ");
			printLinewise(bestMatches);
		});

		DecimalFormat f = new DecimalFormat("0.0");
		System.out.println("Total of " + total.get() + " entries.");
		System.out.println(unique.get() + " unique matches (" + f.format(100.0 * unique.get() / total.get()) + "%)");
		System.out.println(
				ambiguous.get() + " ambiguous matches (" + f.format(100.0 * ambiguous.get() / total.get()) + "%)");
		System.out.println(unmatched.get() + " unmatched (" + f.format(100.0 * unmatched.get() / total.get()) + "%)");
	}

	private static void printLinewise(List<? extends Object> objects) {
		System.out.println("[");
		for (int i = 0; i < objects.size(); i++) {
			System.out.print("    ");
			System.out.print(objects.get(i));
			if (i < objects.size() - 1)
				System.out.print(",");
			System.out.println();
		}
		System.out.println("]");
	}

}
