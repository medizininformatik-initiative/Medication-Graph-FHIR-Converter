package de.medizininformatikinitiative.medgraph.ui.searchengine.query

import de.medizininformatikinitiative.medgraph.searchengine.QueryExecutor
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.RefinedQuery
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Product
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject

/**
 * [QueryExecutor]-implementation which does nothing and provides an empty list as result of every search.
 *
 * @author Markus Budeus
 */
class StubQueryExecutor : QueryExecutor<Product> {
    override fun executeQuery(query: RefinedQuery): List<MatchingObject<Product>> = emptyList()
}
