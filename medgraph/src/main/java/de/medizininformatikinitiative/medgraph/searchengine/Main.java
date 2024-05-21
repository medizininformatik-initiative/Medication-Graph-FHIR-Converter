package de.medizininformatikinitiative.medgraph.searchengine;

import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnection;
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.SimpleQueryExecutor;
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.initial.LevenshteinSearchMatchFinder;
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.refining.ExperimentalRefiner;
import de.medizininformatikinitiative.medgraph.searchengine.db.Neo4jCypherDatabase;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.provider.Providers;

import java.util.Collections;

/**
 * This class currently serves no value other than me playing around and experimenting with things.
 *
 * @author Markus Budeus
 */
public class Main {

	public static void main(String[] args) {
		DatabaseConnection.runSession(session -> {
			LevenshteinSearchMatchFinder matchFinder = new LevenshteinSearchMatchFinder(
					Providers.getProductSynonymes(session),
					Providers.getSubstanceSynonymes(session)
			);

			ExperimentalRefiner refiner = new ExperimentalRefiner(session, new Neo4jCypherDatabase(session));

			SimpleQueryExecutor algorithm = new SimpleQueryExecutor(matchFinder, refiner);
			algorithm.executeQuery(new SearchQuery(null, "Aspirin",
					Collections.emptyList(),
					Collections.emptyList()));
		});
	}

}
