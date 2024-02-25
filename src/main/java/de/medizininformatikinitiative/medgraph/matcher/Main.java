package de.medizininformatikinitiative.medgraph.matcher;

import de.medizininformatikinitiative.medgraph.graphdbpopulator.DatabaseConnection;
import de.medizininformatikinitiative.medgraph.matcher.algorithm.MastersThesisAlgorithm;
import de.medizininformatikinitiative.medgraph.matcher.algorithm.MatchingAlgorithm;
import de.medizininformatikinitiative.medgraph.matcher.data.SubSortingTree;
import de.medizininformatikinitiative.medgraph.matcher.model.HouselistEntry;
import de.medizininformatikinitiative.medgraph.matcher.model.MatchingTarget;
import de.medizininformatikinitiative.medgraph.matcher.model.ProductWithPzn;
import de.medizininformatikinitiative.medgraph.matcher.model.ResultSet;
import de.medizininformatikinitiative.medgraph.matcher.ui.SearchDialog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Main class for the search algorithm.
 *
 * @author Markus Budeus
 */
public class Main {

	public static void main(String[] args) throws IOException {
		if (args.length > 0) {
			switch (args[0]) {
				case "interactive" -> {
					interactive();
				}
				case "ui" -> {
					withUi();
				}
			}
		}
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
					ResultSet<ProductWithPzn> resultSet = Matcher.toResultSet(result, bestMatchTransformer);
					System.out.println("-------- Best match: --------");
					System.out.println(resultSet.primaryResult);
					System.out.println("-------- Good results: --------");
					printLinewise(resultSet.secondaryResults);
					System.out.println("-------- Other results: --------");
					printLinewise(resultSet.tertiaryResults);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

	public static void withUi() {

		SearchDialog.createFrame((dialog, searchTerm) -> {

			DatabaseConnection.runSession(session -> {
				Matcher matcher = new Matcher(session);
				dialog.applyResults(matcher.performMatching(searchTerm));
			});

		});
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
