package de.medizininformatikinitiative.medgraph.searchengine.algorithm;

import de.medizininformatikinitiative.medgraph.searchengine.QueryExecutor;
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.RefinedQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject;
import org.neo4j.driver.Session;

import java.util.List;

/**
 * Query executor which uses the {@link MatchingObject} score variable to keep track of the best matches.
 *
 * @author Markus Budeus
 */
public class WeightedScoringBasedQueryExecutor implements QueryExecutor {

	// TODO Better Javadoc

	private final Session session;

	public WeightedScoringBasedQueryExecutor(Session session) {
		this.session = session;
	}

	@Override
	public List<MatchingObject<?>> executeQuery(RefinedQuery query) {
		// TODO Implement
		return List.of();
	}

}
