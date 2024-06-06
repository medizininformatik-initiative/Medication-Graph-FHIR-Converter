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

        sut.expanded = true
        sut.queryText = "This is a query"
        sut.productQueryText = "Aspirin HEXAL"
        sut.substanceQueryText = "Prednisolon"
        sut.dosageQueryText = "250mg"
        sut.doseFormQueryText = "i.v."

        val query = sut.createQuery()

        assertEquals("This is a query", query.query)
        assertEquals("Aspirin HEXAL", query.product)
        assertEquals("Prednisolon", query.substance)
        assertEquals("250mg", query.dosages)
        assertEquals("i.v.", query.doseForms)
    }

    @Test
    fun createQueryWhileUnexpanded() {
        val sut = QueryViewModel()

        sut.expanded = false // !!
        sut.queryText = "This should apply"
        sut.productQueryText = "This should not apply"
        sut.substanceQueryText = "This should not apply"
        sut.dosageQueryText = "This should not apply"
        sut.doseFormQueryText = "This should not apply"

        val query = sut.createQuery()

        assertEquals("This should apply", query.query)
        assertEquals("", query.product)
        assertEquals("", query.substance)
        assertEquals("", query.dosages)
        assertEquals("", query.doseForms)
    }
}