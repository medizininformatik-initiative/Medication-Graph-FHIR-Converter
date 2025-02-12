package de.medizininformatikinitiative.medgraph.searchengine.algorithm.initial;

import de.medizininformatikinitiative.medgraph.searchengine.matcher.model.Match;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Identifiable;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable;
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.OriginalMatch;

import java.io.IOException;
import java.util.stream.Stream;

/**
 * Class responsible for searching initial matches for a search, which can be narrowed down and refined in subsequent
 * steps.
 *
 * @author Markus Budeus
 */
public interface InitialMatchFinder<T extends Matchable> {

	/**
	 * Searches for initial {@link Matchable}s which relate to the given search term. The nature of the matches is not
	 * defined by this interface, so long as they are {@link Matchable}-instances. Implementations may return multiple
	 * matches referring to the same object if this object is found in multiple ways. Merging of such duplicates is left
	 * to the caller.
	 *
	 * @param query the query to use for searching initial matches
	 * @return a stream providing initial matches which can subsequently be filtered and/or refined
	 */
	Stream<OriginalMatch<T>> findInitialMatches(SearchQuery query);

    void close() throws IOException;
}
