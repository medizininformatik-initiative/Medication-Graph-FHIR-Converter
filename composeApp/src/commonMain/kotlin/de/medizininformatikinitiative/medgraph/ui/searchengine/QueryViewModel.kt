package de.medizininformatikinitiative.medgraph.ui.searchengine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import de.medizininformatikinitiative.medgraph.searchengine.model.RawQuery

/**
 * View model for the query input dialog.
 *
 * @author Markus Budeus
 */
class QueryViewModel {

    var queryText by mutableStateOf("")
    var productQueryText by mutableStateOf("")
    var substanceQueryText by mutableStateOf("")

    fun createQuery() = RawQuery(queryText, productQueryText, substanceQueryText)

}