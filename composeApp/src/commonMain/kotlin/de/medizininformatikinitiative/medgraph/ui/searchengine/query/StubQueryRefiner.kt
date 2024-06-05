package de.medizininformatikinitiative.medgraph.ui.searchengine.query

import de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.QueryRefiner
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.RefinedQuery
import de.medizininformatikinitiative.medgraph.searchengine.model.RawQuery
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery

/**
 * [QueryRefiner]-implementation which always provides the same, useless result.
 *
 * @author Markus Budeus
 */
class StubQueryRefiner : QueryRefiner {
    override fun refine(query: RawQuery): RefinedQuery =
        RefinedQuery(SearchQuery.Builder().build(), null, null, null, null)
}