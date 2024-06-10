package de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement;

import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery;
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.Identifier;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.InputUsageTraceable;
import de.medizininformatikinitiative.medgraph.searchengine.tracing.SubstringUsageStatement;

/**
 * A tool which takes care of partial query refinement, i.e. refining parts of the
 * {@link de.medizininformatikinitiative.medgraph.searchengine.model.RawQuery} to parts of the
 * {@link de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery}.
 *
 * @param <T> the type of result produced by this partial refiner
 * @author Markus Budeus
 */
public interface PartialQueryRefiner<T extends PartialQueryRefiner.Result> {

	/**
	 * Parses the given string identifier query and returns a corresponding result.
	 */
	T parse(Identifier<String> query);

	/**
	 * The result of a partial query refinement. Specifies which part of the passed query was relevant to the resolved
	 * data.
	 */
	interface Result extends InputUsageTraceable<SubstringUsageStatement> {

		/**
		 * <b>Incrementally</b> applies this result to the given search query builder. Incrementally, as in, if
		 * multiple
		 * instances of this result were to be applied to the given builder, each of their contents must be transferred
		 * to the builder. (As opposed to only the last one to be applied counting, as it overwrites whatever previous
		 * results were present.)
		 *
		 * @param searchQueryBuilder the search query builder to which to apply this result
		 */
		void incrementallyApply(SearchQuery.Builder searchQueryBuilder);

	}

}
