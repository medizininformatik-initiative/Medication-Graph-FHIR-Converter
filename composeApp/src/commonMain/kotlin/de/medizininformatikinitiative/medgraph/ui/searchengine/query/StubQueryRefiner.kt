package de.medizininformatikinitiative.medgraph.ui.searchengine.query

import de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.QueryRefiner
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.RefinedQuery
import de.medizininformatikinitiative.medgraph.searchengine.model.RawQuery
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.OriginalIdentifier

/**
 * [QueryRefiner]-implementation which always provides the same, useless result.
 *
 * @author Markus Budeus
 */
class StubQueryRefiner :
    QueryRefiner {
    override fun refine(query: RawQuery): RefinedQuery =
        RefinedQuery.Builder()
            .withProductNameKeywords(OriginalIdentifier(listOf("Aspirin"), OriginalIdentifier.Source.RAW_QUERY))
            .build()
}