package de.medizininformatikinitiative.medgraph.ui.searchengine

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * @author Markus Budeus
 */
class QueryViewModelTest {

    @Test
    fun createQuery() {
        val sut = QueryViewModel()

        sut.queryText.value = "This is a query"
        sut.productQueryText.value = "Aspirin HEXAL"
        sut.substanceQueryText.value = "Prednisolon"

        val query = sut.createQuery()

        assertEquals("This is a query", query.query)
        assertEquals("Aspirin HEXAL", query.product)
        assertEquals("Prednisolon", query.substance)
    }
}