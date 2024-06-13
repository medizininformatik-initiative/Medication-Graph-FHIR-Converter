package de.medizininformatikinitiative.medgraph.ui.searchengine.query

import de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.NewQueryRefiner
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.NewRefinedQuery
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.RefinedQuery
import de.medizininformatikinitiative.medgraph.searchengine.model.RawQuery
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.OriginalIdentifier

/**
 * [NewQueryRefiner]-implementation which always provides the same, useless result.
 *
 * @author Markus Budeus
 */
class NewStubQueryRefiner : NewQueryRefiner {
    override fun refine(query: RawQuery): NewRefinedQuery =
        NewRefinedQuery.Builder()
            .withProductNameKeywords(OriginalIdentifier(listOf("Aspirin"), OriginalIdentifier.Source.RAW_QUERY))
            .build()
}