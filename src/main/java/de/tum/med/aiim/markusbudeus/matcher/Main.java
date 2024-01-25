package de.tum.med.aiim.markusbudeus.matcher;

import de.tum.med.aiim.markusbudeus.graphdbpopulator.DatabaseConnection;
import de.tum.med.aiim.markusbudeus.matcher.algorithm.MatchingAlgorithm;
import de.tum.med.aiim.markusbudeus.matcher.algorithm.MastersThesisAlgorithm;
import de.tum.med.aiim.markusbudeus.matcher.data.SubSortingTree;
import de.tum.med.aiim.markusbudeus.matcher.model.ProductWithPzn;
import de.tum.med.aiim.markusbudeus.matcher.model.HouselistEntry;
import de.tum.med.aiim.markusbudeus.matcher.model.MatchingTarget;
import de.tum.med.aiim.markusbudeus.matcher.sample.synthetic.HouselistMatcher;
import de.tum.med.aiim.markusbudeus.matcher.sample.synthetic.SyntheticHouselistEntry;
import de.tum.med.aiim.markusbudeus.matcher.ui.SearchDialog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

	public static void main(String[] args) throws IOException {
		if (args.length > 0) {
			switch (args[0]) {
				case "interactive" -> {
					interactive();
					return;
				}
				case "ui" -> {
					withUi();
					return;
				}
			}
		}

		List<SyntheticHouselistEntry> entries = HouselistMatcher.loadHouselist();

		DatabaseConnection.runSession(session -> {
			MatchingAlgorithm algorithm = new MastersThesisAlgorithm(session);
			BestMatchTransformer bestMatchTransformer = new BestMatchTransformer(session);

			processStreamAndPrintResults(entries.stream()
			                                    .parallel()
			                                    .map(e -> new MatchingResult(e, algorithm.match(e))),
					bestMatchTransformer);
		});
	}

	public static void interactive() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		DatabaseConnection.runSession(session -> {
			MatchingAlgorithm algorithm = new MastersThesisAlgorithm(session);
			BestMatchTransformer bestMatchTransformer = new BestMatchTransformer(session);

			while (!Thread.interrupted()) {
				try {
					System.out.print("> ");
					String line = reader.readLine();
					HouselistEntry entry = new HouselistEntry();
					entry.searchTerm = line;
					SubSortingTree<MatchingTarget> result = algorithm.match(entry);
					ResultSet resultSet = toResultSet(result, bestMatchTransformer);
					System.out.println("-------- Best match: --------");
					System.out.println(resultSet.bestResult);
					System.out.println("-------- Good results: --------");
					printLinewise(resultSet.goodResults);
					System.out.println("-------- Other results: --------");
					printLinewise(resultSet.otherResults);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

	public static void withUi() {

		SearchDialog.createFrame((dialog, searchTerm) -> {

			DatabaseConnection.runSession(session -> {
				MatchingAlgorithm algorithm = new MastersThesisAlgorithm(session);
				BestMatchTransformer bestMatchTransformer = new BestMatchTransformer(session);
				HouselistEntry entry = new HouselistEntry();
				entry.searchTerm = searchTerm;
				SubSortingTree<MatchingTarget> result = algorithm.match(entry);
				ResultSet resultSet = toResultSet(result, bestMatchTransformer);
				dialog.applyResults(resultSet);
			});

		});
	}

	private static void processStreamAndPrintResults(Stream<MatchingResult> stream, BestMatchTransformer bestMatchTransformer) {
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

			System.out.println(result.searchTerm.searchTerm + " -> " + bestMatchTransformer.reorderAndTransform(bestMatches));
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

	public static ResultSet toResultSet(SubSortingTree<MatchingTarget> results, BestMatchTransformer bestMatchTransformer) {
		List<MatchingTarget> topResults = results.getTopContents();
		List<MatchingTarget> otherResults = results.getContents();

		if (otherResults.isEmpty()) return new ResultSet(null, List.of(), List.of());

		otherResults = otherResults.subList(topResults.size(), otherResults.size());
		List<ProductWithPzn> transformedTargets = bestMatchTransformer.reorderAndTransform(results.getContents());

		Set<Long> topMmiIds = topResults.stream().map(MatchingTarget::getMmiId).collect(Collectors.toSet());

		List<ProductWithPzn> sortedTransformedTopTargets = new ArrayList<>(topResults.size());
		for (ProductWithPzn target: transformedTargets) {
			if (topMmiIds.contains(target.getMmiId())) {
				sortedTransformedTopTargets.add(target);
			}
		}
		List<ProductWithPzn> transformedOtherTargets = new ArrayList<>(otherResults.size());
		for (MatchingTarget target: otherResults) {
			for (ProductWithPzn t: transformedTargets) {
				if (t.getMmiId() == target.getMmiId()) {
					transformedOtherTargets.add(t);
				}
			}
		}

		ProductWithPzn best = sortedTransformedTopTargets.get(0);
		sortedTransformedTopTargets.remove(0);

		return new ResultSet(best, sortedTransformedTopTargets, transformedOtherTargets);
	}

	public static class ResultSet {
		public final ProductWithPzn bestResult;
		public final List<ProductWithPzn> goodResults;
		public final List<ProductWithPzn> otherResults;

		private ResultSet(ProductWithPzn bestResult, List<ProductWithPzn> goodResults,
		                  List<ProductWithPzn> otherResults) {
			this.bestResult = bestResult;
			this.goodResults = goodResults;
			this.otherResults = otherResults;
		}
	}

}
