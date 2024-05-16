package de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement;

import de.medizininformatikinitiative.medgraph.searchengine.model.RawQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import org.jetbrains.annotations.NotNull;

/**
 * A QueryParser is responsible for taking a user-provided {@link RawQuery} and transforming it into a
 * {@link SearchQuery}.
 *
 * @author Markus Budeus
 */
public interface QueryParser {

	/**
	 * Parses the given user-provided query into the more easily usable {@link SearchQuery} object.
	 *
	 * @param query the query to parse
	 * @return the parsed Query
	 */
	@NotNull
	SearchQuery parse(@NotNull RawQuery query);

}
