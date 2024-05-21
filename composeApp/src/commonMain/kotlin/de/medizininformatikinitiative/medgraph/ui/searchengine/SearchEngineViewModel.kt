package de.medizininformatikinitiative.medgraph.ui.searchengine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.QueryParser
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.SimpleQueryParser
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery

/**
 * View model for the full search engine screen.
 *
 * @author Markus Budeus
 */
class SearchEngineViewModel(
    val queryParser: QueryParser = SimpleQueryParser()
) : ScreenModel {

    val queryViewModel = QueryViewModel()

    /**
     * The current parsed query.
     */
    var parsedQuery by mutableStateOf<SearchQuery?>(null)

    fun parseQuery() {
        parsedQuery = queryParser.parse(queryViewModel.createQuery())
    }

}