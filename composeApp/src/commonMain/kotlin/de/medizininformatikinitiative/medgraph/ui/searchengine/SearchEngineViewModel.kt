package de.medizininformatikinitiative.medgraph.ui.searchengine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import de.medizininformatikinitiative.medgraph.searchengine.QueryExecutor
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.QueryRefiner
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.RefinedQuery
import de.medizininformatikinitiative.medgraph.ui.searchengine.query.StubQueryRefiner
import de.medizininformatikinitiative.medgraph.ui.searchengine.query.QueryViewModel
import de.medizininformatikinitiative.medgraph.ui.searchengine.query.StubQueryExecutor
import de.medizininformatikinitiative.medgraph.ui.searchengine.results.SearchResultsListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * View model for the full search engine screen.
 *
 * @author Markus Budeus
 */
open class SearchEngineViewModel(
    private val queryRefiner: QueryRefiner = StubQueryRefiner(),
    private val queryExecutor: QueryExecutor = StubQueryExecutor()
) : ScreenModel {

    val queryViewModel = QueryViewModel()

    /**
     * The current refined query.
     */
    var refinedQuery by mutableStateOf<RefinedQuery?>(null)
        private set

    /**
     * Whether a query is currently being executed.
     */
    var queryExecutionUnderway by mutableStateOf(false)
        private set

    val resultsViewModel = SearchResultsListViewModel()

    var queryRefiningUnderway by mutableStateOf(false)
        private set

    /**
     * Whether this view model is currently busy refining or executing a query.
     */
    var busy by mutableStateOf(false)
        private set

    /**
     * Parses the query as given in the [queryViewModel].
     */
    fun refineQuery(): Job? = requestAsyncAction { syncRefineQuery() }

    /**
     * Executes the currently parsed query. Has no effect if there is no parsed query currently.
     * Has also no effect if a query is currently being processed.
     *
     * @return if a new execution job started, the job, otherwise null
     */
    fun executeQuery(): Job? = requestAsyncAction { syncExecuteQuery() }

    /**
     * Parses the current query and then executes it.
     *
     * @return the job representing the query execution or null if the query execution could not start successfully
     */
    fun refineAndExecuteQuery(): Job? = requestAsyncAction { syncRefineQuery(); syncExecuteQuery() }

    private fun syncRefineQuery() {
        try {
            queryRefiningUnderway = true
            val refinedQuery = queryRefiner.refine(queryViewModel.createQuery());
            this.refinedQuery = refinedQuery
            queryViewModel.applyRefiningResult(refinedQuery)
        } finally {
            queryRefiningUnderway = false
        }
    }

    private fun syncExecuteQuery() {
        queryExecutionUnderway = true
        try {
            resultsViewModel.clearResults()
            val query = refinedQuery?.toSearchQuery()
            if (query == null) return
            resultsViewModel.assignResults(queryExecutor.executeQuery(query))
        } finally {
            queryExecutionUnderway = false
        }
    }

    private fun requestAsyncAction(action: suspend () -> Unit): Job? {
        if (busy) {
            return null;
        }
        synchronized(this) {
            if (busy) {
                return null;
            }
            busy = true;
        }
        return screenModelScope.launch(Dispatchers.IO) {
            try {
                action()
            } finally {
                busy = false
            }
        }
    }

}
