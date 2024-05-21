package de.medizininformatikinitiative.medgraph.ui.searchengine

import de.medizininformatikinitiative.medgraph.searchengine.QueryExecutor

/**
 * @author Markus Budeus
 */
class LiveSearchEngineViewModel : SearchEngineViewModel(
    queryExecutor = buildQueryExecutor()
) {

    private fun buildQueryExecutor(): QueryExecutor {

    }


}