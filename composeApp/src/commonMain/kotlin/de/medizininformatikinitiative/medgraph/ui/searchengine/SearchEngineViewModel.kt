package de.medizininformatikinitiative.medgraph.ui.searchengine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import de.medizininformatikinitiative.medgraph.searchengine.QueryExecutor
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.QueryParser
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.SimpleQueryParser
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.launch

/**
 * View model for the full search engine screen.
 *
 * @author Markus Budeus
 */
open class SearchEngineViewModel(
    private val queryParser: QueryParser = SimpleQueryParser(),
    private val queryExecutor: QueryExecutor = StubQueryExecutor()
) : ScreenModel {

    val queryViewModel = QueryViewModel()

    private val parsedQueryState = mutableStateOf<SearchQuery?>(null)

    /**
     * The current parsed query.
     */
    val parsedQuery by parsedQueryState

    private val queryExecutionState = mutableStateOf(false)

    /**
     * Whether a query is currently being executed.
     */
    val queryExecutionUnderway by queryExecutionState

    private val queryResultsState = mutableStateOf<List<MatchingObject>>(emptyList())

    /**
     * The results of the most recent query.
     */
    val queryResults by queryResultsState

    /**
     * Parses the query as given in the [queryViewModel].
     */
    fun parseQuery() {
        parsedQueryState.value = queryParser.parse(queryViewModel.createQuery())
    }

    /**
     * Executes the currently parsed query. Has no effect if there is no parsed query currently.
     * Has also no effect if a query is currently being processed.
     *
     * @return if a new execution job started, the job, otherwise null
     */
    fun executeQuery(): Job? {
        synchronized(this) {
            if (queryExecutionUnderway) return null
            queryExecutionState.value = true
        }
        val query = parsedQuery
        queryResultsState.value = emptyList()
        if  (query == null) return null
        return screenModelScope.launch (Dispatchers.IO) {
            try {
                queryResultsState.value = queryExecutor.executeQuery(query)
            } finally {
                queryExecutionState.value = false
            }
        }
    }

}
