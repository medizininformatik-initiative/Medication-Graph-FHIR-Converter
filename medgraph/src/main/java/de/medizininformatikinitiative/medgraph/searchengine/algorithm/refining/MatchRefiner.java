package de.medizininformatikinitiative.medgraph.searchengine.algorithm.refining;

import de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.RefinedQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject;
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.tree.SubSortingTree;

import java.util.List;

/**
 * A match refiner takes the results from an initial search and, by using filtering and rating techniques, selects the
 * best matches for a given query.
 *
 * @author Markus Budeus
 */
public interface MatchRefiner {

	/**
	 * Analyzes the given initial matches and, using information from the search query, removes unfitting matches and
	 * sorts the remaining matches according to different metrics. The resulting matches are returned in a
	 * {@link SubSortingTree}, which retains information about the sorting steps applied.
	 * <p>
	 * The provided initial matches should be duplicate-free, possible duplicates should be merged before being passed
	 * to this method.
	 *
	 * @param initialMatches the initial matches which to refine
	 * @param query          the query to consider for the refinement
	 * @return a {@link SubSortingTree} containing the refined matches, sorted by relevance
	 */
	SubSortingTree<MatchingObject<?>> refineMatches(List<? extends MatchingObject<?>> initialMatches, RefinedQuery query);

}
