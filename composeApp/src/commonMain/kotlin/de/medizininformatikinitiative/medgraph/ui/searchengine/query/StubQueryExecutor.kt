package de.medizininformatikinitiative.medgraph.ui.searchengine.query

import de.medizininformatikinitiative.medgraph.searchengine.QueryExecutor
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject

/**
 * [QueryExecutor]-implementation which does nothing and provides an empty list as result of every search.
 *
 * @author Markus Budeus
 */
class StubQueryExecutor : QueryExecutor {
    override fun executeQuery(query: SearchQuery): List<MatchingObject> = emptyList()
}
