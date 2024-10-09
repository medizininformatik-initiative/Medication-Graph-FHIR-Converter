package de.medizininformatikinitiative.medgraph.searchengine.algorithm;

import de.medizininformatikinitiative.medgraph.searchengine.QueryExecutor;
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.initial.ApacheLuceneInitialMatchFinder;
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.initial.InitialMatchFinder;
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.RefinedQuery;
import de.medizininformatikinitiative.medgraph.searchengine.db.Neo4jCypherDatabase;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.DetailedProduct;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Product;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.ScoreMergingStrategy;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.MatchingPipelineService;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.transformer.ProductDetailsResolver;
import org.neo4j.driver.Session;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Query executor relying fully on the {@link ApacheLuceneInitialMatchFinder}. Apart from resolving product details
 * for the found products, this executor does nothing.
 */
public class ApacheLuceneQueryExecutor implements QueryExecutor<DetailedProduct> {

	private final InitialMatchFinder<Product> initialMatchFinder;

	private final ProductDetailsResolver productDetailsResolver;

	public ApacheLuceneQueryExecutor(Session session) {
		initialMatchFinder = new ApacheLuceneInitialMatchFinder(session);
		productDetailsResolver = new ProductDetailsResolver(new Neo4jCypherDatabase(session));
	}

	@Override
	public List<MatchingObject<DetailedProduct>> executeQuery(RefinedQuery query) {
		SearchQuery searchQuery = query.toSearchQuery();
		MatchingPipelineService service = new MatchingPipelineService(searchQuery);
		List<MatchingObject<Product>> products = initialMatchFinder.findInitialMatches(searchQuery)
		                                                           .collect(Collectors.toList());
		products = service.mergeDuplicates(products, ScoreMergingStrategy.MAX);
		List<? extends MatchingObject<DetailedProduct>> detailedProducts = service.transformMatches(products,
				productDetailsResolver, ScoreMergingStrategy.MAX);
		detailedProducts.sort(Comparator.naturalOrder());
		return new ArrayList<>(detailedProducts);
	}
}