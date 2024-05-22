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

    companion object {
        val MAX_RESULT_SIZE = 100
    }

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

    private val actualLastQueryResultSizeState = mutableStateOf<Int?>(null)

    /**
     * The actual result size of the last query, whose result has been cropped because it is too large.
     * If there is no last query or it has not been cropped, this is null.
     */
    var actualLastQueryResultSize by actualLastQueryResultSizeState
        private set
    private var lastActualQueryResult: List<MatchingObject> = emptyList()
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
        actualLastQueryResultSize = null
        if  (query == null) return null
        return screenModelScope.launch (Dispatchers.IO) {
            try {
                lastActualQueryResult = queryExecutor.executeQuery(query)
                if (lastActualQueryResult.size <= MAX_RESULT_SIZE) queryResultsState.value = lastActualQueryResult
                else {
                    queryResultsState.value = lastActualQueryResult.subList(0, MAX_RESULT_SIZE)
                    actualLastQueryResultSize = lastActualQueryResult.size
                }
            } finally {
                queryExecutionState.value = false
            }
        }
    }

    /**
     * Parses the current query and then executes it.
     *
     * @return the job representing the query execution or null if the query execution could not start successfully
     */
    fun parseAndExecuteQuery(): Job? {
        parseQuery()
        return executeQuery()
    }

}
