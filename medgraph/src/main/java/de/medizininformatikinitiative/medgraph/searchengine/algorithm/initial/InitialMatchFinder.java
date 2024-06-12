package de.medizininformatikinitiative.medgraph.searchengine.algorithm.initial;

import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.OriginalMatch;

import java.util.stream.Stream;

/**
 * Class responsible for searching initial matches for a search, which can be narrowed down and refined in subsequent
 * steps.
 *
 * @author Markus Budeus
 */
public interface InitialMatchFinder {

	/**
	 * Searches for initial {@link Matchable}s which relate to the given search term. The nature of the matches is not
	 * defined by this interface, so long as they are {@link Matchable}-instances.
	 *
	 * @param query the query to use for searching initial matches
	 * @return a stream providing initial matches which can subsequently be filtered and/or refined
	 */
	Stream<OriginalMatch<?>> findInitialMatches(SearchQuery query);

}
