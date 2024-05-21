package de.medizininformatikinitiative.medgraph.ui.searchengine

import cafe.adriel.voyager.core.model.screenModelScope
import de.medizininformatikinitiative.medgraph.searchengine.QueryExecutor
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.QueryParser
import de.medizininformatikinitiative.medgraph.searchengine.model.RawQuery
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.OriginalMatch
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Product
import de.medizininformatikinitiative.medgraph.ui.UnitTest
import kotlinx.coroutines.job
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito.*

/**
 * @author Markus Budeus
 */
class SearchEngineViewModelTest : UnitTest() {

    @Mock
    lateinit var queryParser: QueryParser
    @Mock
    lateinit var queryExecutor: QueryExecutor
    @Mock
    lateinit var sampleQuery: SearchQuery

    val sampleSearchResult: List<MatchingObject> = listOf(OriginalMatch(Product(1, "Aspirin")))

    private lateinit var sut: SearchEngineViewModel

    @BeforeEach
    fun setUp() {
        `when`(queryParser.parse(any())).thenReturn(sampleQuery)
        `when`(queryExecutor.executeQuery(any())).thenReturn(sampleSearchResult)
        sut = SearchEngineViewModel(queryParser, queryExecutor)
    }

    @Test
    fun parseRawQuery() {
        sut.queryViewModel.queryText = "Query"
        sut.queryViewModel.productQueryText = "Prod"
        sut.queryViewModel.substanceQueryText = "Subst"
        sut.parseQuery()

        verify(queryParser).parse(ArgumentMatchers.eq(RawQuery("Query", "Prod", "Subst")))
        assertEquals(sampleQuery, sut.parsedQuery)
    }

    @Test
    fun executeQuery() {
        sut.parseQuery()
        runBlocking {
            sut.executeQuery()!!.join()
        }

        assertFalse(sut.queryExecutionUnderway)
        assertEquals(sampleSearchResult, sut.queryResults)
    }

    @Test
    fun executeFailingQuery() {
        `when`(queryExecutor.executeQuery(any())).thenThrow(RuntimeException("This went horribly wrong."))
        sut.parseQuery()
        runBlocking {
            sut.executeQuery()!!.join()
        }

        assertFalse(sut.queryExecutionUnderway)
        assertEquals(emptyList<MatchingObject>(), sut.queryResults)
    }

}