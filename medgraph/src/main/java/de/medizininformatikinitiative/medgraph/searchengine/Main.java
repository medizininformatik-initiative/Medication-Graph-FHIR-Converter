package de.medizininformatikinitiative.medgraph.searchengine;

import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnection;
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.initial.LevenshteinSearchMatchFinder;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.provider.Providers;
import org.neo4j.driver.Session;

/**
 * This class currently serves no value other than me playing around and experimenting with things.
 *
 * @author Markus Budeus
 */
public class Main {

	public static void main(String[] args) {
		try (DatabaseConnection connection = new DatabaseConnection(); Session session = connection.createSession()) {

			LevenshteinSearchMatchFinder matchFinder = new LevenshteinSearchMatchFinder(
					Providers.getProductSynonymes(session),
					Providers.getSubstanceSynonymes(session)
			);

			matchFinder.findInitialMatches(new SearchQuery(null, "Aspirin"))
					.forEach(System.out::println);
		}
	}

}
