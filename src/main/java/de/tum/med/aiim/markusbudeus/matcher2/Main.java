package de.tum.med.aiim.markusbudeus.matcher2;

import de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseConnection;
import de.tum.med.aiim.markusbudeus.matcher2.algorithms.MatchingAlgorithm;
import de.tum.med.aiim.markusbudeus.matcher2.algorithms.SampleAlgorithm;
import de.tum.med.aiim.markusbudeus.matcher2.data.SubSortingTree;
import de.tum.med.aiim.markusbudeus.matcher2.model.MatchingTarget;
import de.tum.med.aiim.markusbudeus.matcher2.sample.synthetic.HouselistMatcher;
import de.tum.med.aiim.markusbudeus.matcher2.sample.synthetic.SyntheticHouselistEntry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class Main {

	public static void main(String[] args) throws IOException {

		if (args.length > 0) {
			if ("interactive".equals(args[0])) {
				interactive();
				return;
			}
		}
		List<SyntheticHouselistEntry> entries = HouselistMatcher.loadHouselist();

		DatabaseConnection.runSession(session -> {
			MatchingAlgorithm algorithm = new SampleAlgorithm(session);

			processStreamAndPrintResults(entries.stream()
			                                    .parallel()
			                                    .map(e -> new MatchingResult(e, algorithm.match(e))));
		});

	}

	public static void interactive() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		DatabaseConnection.runSession(session -> {
			MatchingAlgorithm algorithm = new SampleAlgorithm(session);

			while (!Thread.interrupted()) {
				try {
					System.out.print("> ");
					String line = reader.readLine();
					HouselistEntry entry = new HouselistEntry();
					entry.name = line;
					SubSortingTree<MatchingTarget> resultTree = algorithm.match(entry);
					List<MatchingTarget> topContents = resultTree.getTopContents();
					List<MatchingTarget> contents = resultTree.getContents();
					System.out.println("-------- Top results: --------");
					printLinewise(topContents);
					System.out.println("-------- Other results: --------");
					printLinewise(contents.subList(topContents.size(), contents.size()));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

	private static void processStreamAndPrintResults(Stream<MatchingResult> stream) {
		AtomicInteger total = new AtomicInteger();
		AtomicInteger unique = new AtomicInteger();
		AtomicInteger ambiguous = new AtomicInteger();
		AtomicInteger unmatched = new AtomicInteger();
		stream.forEach(result -> {
			total.getAndIncrement();
			List<MatchingTarget> bestMatches = result.result.getTopContents();

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

	private static void printLinewise(List<?> objects) {
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
