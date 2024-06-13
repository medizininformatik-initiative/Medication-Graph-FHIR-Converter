package de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement;

import de.medizininformatikinitiative.medgraph.searchengine.model.RawQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;

/**
 * Query refiner capable of turning a {@link RawQuery} into a {@link NewRefinedQuery}.
 *
 * @author Markus Budeus
 */
public interface NewQueryRefiner {

	/**
	 * Refines the given query. The returned {@link NewRefinedQuery} can generate the {@link SearchQuery} and holds
	 * information about which parts of the raw query were used and for what.
	 *
	 * @param query the raw query to refine.
	 * @return a {@link NewRefinedQuery} holding the result {@link SearchQuery} and additional information
	 */
	NewRefinedQuery refine(RawQuery query);

}
