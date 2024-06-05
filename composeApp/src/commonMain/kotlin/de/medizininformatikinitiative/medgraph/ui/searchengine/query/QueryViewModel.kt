package de.medizininformatikinitiative.medgraph.ui.searchengine.query

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
    var dosageQueryText by mutableStateOf("")
    var doseFormQueryText by mutableStateOf("")

    fun createQuery() = RawQuery(queryText, productQueryText, substanceQueryText, dosageQueryText, doseFormQueryText)

}