package de.medizininformatikinitiative.medgraph.ui.searchengine.query

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * @author Markus Budeus
 */
class QueryViewModelTest {

    @Test
    fun createQuery() {
        val sut = QueryViewModel()

        sut.queryText = "This is a query"
        sut.productQueryText = "Aspirin HEXAL"
        sut.substanceQueryText = "Prednisolon"

        val query = sut.createQuery()

        assertEquals("This is a query", query.query)
        assertEquals("Aspirin HEXAL", query.product)
        assertEquals("Prednisolon", query.substance)
    }
}