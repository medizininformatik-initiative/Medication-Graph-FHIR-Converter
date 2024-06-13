package de.medizininformatikinitiative.medgraph.ui.searchengine.query

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.NewRefinedQuery
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.RefinedQuery
import de.medizininformatikinitiative.medgraph.searchengine.model.RawQuery
import de.medizininformatikinitiative.medgraph.searchengine.tracing.SubstringUsageStatement

/**
 * View model for the query input dialog.
 *
 * @author Markus Budeus
 */
class QueryViewModel {

    /**
     * Whether additional search query fields besides queryText shall be shown.
     */
    var expanded by mutableStateOf(false)

    var queryText by mutableStateOf("")
    var productQueryText by mutableStateOf("")
    var substanceQueryText by mutableStateOf("")
    var dosageQueryText by mutableStateOf("")
    var doseFormQueryText by mutableStateOf("")

    var dosageGeneralSearchTermUsageStatement by mutableStateOf<SubstringUsageStatement?>(null)
    var doseFormGeneralSearchTermUsageStatement by mutableStateOf<SubstringUsageStatement?>(null)
    var dosageUsageStatement by mutableStateOf<SubstringUsageStatement?>(null)
    var doseFormUsageStatement by mutableStateOf<SubstringUsageStatement?>(null)

    fun createQuery(): RawQuery {
        if (!expanded) {
            return RawQuery(queryText)
        } else {
            return RawQuery(queryText, productQueryText, substanceQueryText, dosageQueryText, doseFormQueryText)
        }
    }

    /**
     * Applies the given refining result to this view model, allowing it to color the query texts according to how
     * they were used in the given refined query.
     */
    fun applyRefiningResult(refinedQuery: NewRefinedQuery) {
        dosageGeneralSearchTermUsageStatement = refinedQuery.dosageGeneralSearchTermUsageStatement
        doseFormGeneralSearchTermUsageStatement = refinedQuery.doseFormGeneralSearchTermUsageStatement
        dosageUsageStatement = refinedQuery.dosageUsageStatement
        doseFormUsageStatement = refinedQuery.doseFormUsageStatement
    }

}