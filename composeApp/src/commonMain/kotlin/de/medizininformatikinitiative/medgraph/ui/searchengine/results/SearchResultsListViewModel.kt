package de.medizininformatikinitiative.medgraph.ui.searchengine.results

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject

/**
 * View model for the query results ui.
 *
 * @author Markus Budeus
 */
class SearchResultsListViewModel {

    companion object {
        /**
         * The maximum number of results displayed at once.
         */
        val MAX_RESULT_SIZE = 100
    }

    private val actualLastQueryResultSizeState = mutableStateOf<Int?>(null)

    /**
     * The actual result size of the last query, whose result has been cropped because it is too large.
     * If there is no last query or it has not been cropped, this is null.
     */
    var actualLastQueryResultSize by actualLastQueryResultSizeState
        private set
    private var actualQueryResult: List<MatchingObject<*>> = emptyList()
    private val queryResultsState = mutableStateOf<List<MatchingObject<*>>>(emptyList())

    /**
     * The currently displayed query results.
     */
    var queryResults by queryResultsState
    private set

    /**
     * Assigns the given result list to this ui.
     */
    fun assignResults(results: List<MatchingObject<*>>) {
        if (results.size <= MAX_RESULT_SIZE) queryResults = results
        else {
            queryResultsState.value = results.subList(0, MAX_RESULT_SIZE)
            actualLastQueryResultSize = results.size
        }
    }

    /**
     * Clears the result list.
     */
    fun clearResults() {
        actualLastQueryResultSize = null
        actualQueryResult = emptyList()
        queryResults = emptyList()
    }

}