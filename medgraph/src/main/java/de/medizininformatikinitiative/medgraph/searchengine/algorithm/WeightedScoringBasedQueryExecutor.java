package de.medizininformatikinitiative.medgraph.searchengine.algorithm;

import de.medizininformatikinitiative.medgraph.DI;
import de.medizininformatikinitiative.medgraph.searchengine.QueryExecutor;
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.initial.InitialMatchFinder;
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.initial.LevenshteinSearchMatchFinder;
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.RefinedQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Product;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.OriginalMatch;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.MatchingPipelineService;
import de.medizininformatikinitiative.medgraph.searchengine.provider.Providers;
import org.neo4j.driver.Session;

import java.util.List;
import java.util.stream.Stream;

/**
 * Query executor which uses the {@link MatchingObject} score variable to keep track of the best matches.
 *
 * @author Markus Budeus
 */
public class WeightedScoringBasedQueryExecutor implements QueryExecutor {

	// TODO Better Javadoc

	private final Session session;

	private final InitialMatchFinder<Product> initialMatchFinder;

	public WeightedScoringBasedQueryExecutor(Session session) {
		this.session = session;
		initialMatchFinder = new LevenshteinSearchMatchFinder(
				Providers.getProductSynonyms(session)
		);
	}

	@Override
	public List<MatchingObject<?>> executeQuery(RefinedQuery query) {
		SearchQuery searchQuery = query.toSearchQuery();
		MatchingPipelineService matchingPipelineService = new MatchingPipelineService(searchQuery);
		Stream<OriginalMatch<Product>> products = initialMatchFinder.findInitialMatches(searchQuery);


		// TODO Implement
		return List.of();
	}

}
