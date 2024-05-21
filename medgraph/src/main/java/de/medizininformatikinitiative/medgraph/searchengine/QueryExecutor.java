package de.medizininformatikinitiative.medgraph.searchengine;

import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject;

import java.util.List;

/**
 * A query executor is a class which executes a search query and returns the (refined) results as list of
 * {@link MatchingObject}.
 *
 * @author Markus Budeus
 */
public interface QueryExecutor {

	/**
	 * Runs a search using the given query and returnes the (refined) results, with the elements in the result list
	 * being ordered by relevance in descending order.
	 * @param query the query to use for the search
	 * @return the refined results in descending order of relevance
	 */
	List<MatchingObject> executeQuery(SearchQuery query);

}
