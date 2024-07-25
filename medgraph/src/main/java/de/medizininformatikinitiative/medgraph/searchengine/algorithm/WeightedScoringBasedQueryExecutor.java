package de.medizininformatikinitiative.medgraph.searchengine.algorithm;

import de.medizininformatikinitiative.medgraph.searchengine.QueryExecutor;
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.RefinedQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject;

import java.util.List;

/**
 * Query executor which uses the {@link MatchingObject} score variable too keep track of the best matches.
 *
 * @author Markus Budeus
 */
public class WeightedScoringBasedQueryExecutor implements QueryExecutor {

	@Override
	public List<MatchingObject<?>> executeQuery(RefinedQuery query) {
		// TODO Implement
		return List.of();
	}

}
