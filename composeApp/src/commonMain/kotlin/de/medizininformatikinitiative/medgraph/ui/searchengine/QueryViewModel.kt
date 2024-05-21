package de.medizininformatikinitiative.medgraph.ui.searchengine

import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.ScreenModel
import de.medizininformatikinitiative.medgraph.searchengine.model.RawQuery

/**
 * View model for the query input dialog.
 *
 * @author Markus Budeus
 */
class QueryViewModel {

    val queryText = mutableStateOf("")
    val productQueryText = mutableStateOf("")
    val substanceQueryText = mutableStateOf("")

    fun createQuery() = RawQuery(queryText.value, productQueryText.value, substanceQueryText.value)

}