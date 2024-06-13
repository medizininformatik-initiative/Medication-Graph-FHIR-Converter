package de.medizininformatikinitiative.medgraph.ui.searchengine

import de.medizininformatikinitiative.medgraph.searchengine.QueryExecutor
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.NewQueryRefiner
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.NewRefinedQuery
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.RefinedQuery
import de.medizininformatikinitiative.medgraph.searchengine.model.RawQuery
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Product
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.OriginalMatch
import de.medizininformatikinitiative.medgraph.ui.UnitTest
import de.medizininformatikinitiative.medgraph.ui.searchengine.results.SearchResultsListViewModel
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Markus Budeus
 */
class SearchEngineViewModelTest : UnitTest() {

    @Mock
    lateinit var queryRefiner: NewQueryRefiner
    @Mock
    lateinit var queryExecutor: QueryExecutor
    @Mock
    lateinit var refinedQuery: NewRefinedQuery
    @Mock
    lateinit var sampleQuery: SearchQuery

    val sampleSearchResult: List<MatchingObject<*>> = listOf(OriginalMatch(
        Product(
            1,
            "Aspirin"
        )
    ))

    private lateinit var sut: SearchEngineViewModel

    @BeforeEach
    fun setUp() {
        `when`(refinedQuery.toSearchQuery()).thenReturn(sampleQuery)
        `when`(queryRefiner.refine(any())).thenReturn(refinedQuery)
        `when`(queryExecutor.executeQuery(any())).thenReturn(sampleSearchResult)
        sut = SearchEngineViewModel(queryRefiner, queryExecutor)
        sut.queryViewModel.expanded = true
    }

    @Test
    fun parseRawQuery() {
        sut.queryViewModel.queryText = "Query"
        sut.queryViewModel.productQueryText = "Prod"
        sut.queryViewModel.substanceQueryText = "Subst"
        sut.queryViewModel.dosageQueryText = "Dosage"
        sut.queryViewModel.doseFormQueryText = "Df"
        runBlocking {
            sut.refineQuery()!!.join()
        }

        verify(queryRefiner).refine(ArgumentMatchers.eq(RawQuery("Query", "Prod", "Subst", "Dosage", "Df")))
        assertEquals(refinedQuery, sut.refinedQuery)
    }

    @Test
    fun executeQuery() {
        runBlocking {
            sut.refineQuery()!!.join()
            sut.executeQuery()!!.join()
        }

        assertFalse(sut.busy)
        assertFalse(sut.queryRefiningUnderway)
        assertFalse(sut.queryExecutionUnderway)
        assertNull(sut.resultsViewModel.actualLastQueryResultSize)
        assertEquals(sampleSearchResult, sut.resultsViewModel.queryResults)
    }

    @Test
    fun executeFailingRefinement() {
        `when`(queryRefiner.refine(any())).thenThrow(RuntimeException("This went horribly wrong."))

        runBlocking {
            sut.refineQuery()!!.join()
        }

        assertFalse(sut.busy)
        assertFalse(sut.queryRefiningUnderway)
        assertNull(sut.refinedQuery)
    }

    @Test
    fun executeFailingQuery() {
        `when`(queryExecutor.executeQuery(any())).thenThrow(RuntimeException("This went horribly wrong."))
        runBlocking {
            sut.refineAndExecuteQuery()!!.join()
        }

        assertFalse(sut.busy)
        assertFalse(sut.queryExecutionUnderway)
        assertEquals(emptyList<MatchingObject<*>>(), sut.resultsViewModel.queryResults)
    }

    @Test
    fun refineAndExecute() {
        val currentSearchQuery = mock(SearchQuery::class.java)
        val currentRefinedQuery = mock(NewRefinedQuery::class.java)
        val currentResult = listOf(OriginalMatch(
            Product(
                2,
                "Novalgin"
            )
        ))

        `when`(queryRefiner.refine(any())).thenReturn(currentRefinedQuery)
        `when`(currentRefinedQuery.toSearchQuery()).thenReturn(currentSearchQuery)
        `when`(queryExecutor.executeQuery(currentSearchQuery)).thenReturn(currentResult)

        runBlocking {
            sut.refineAndExecuteQuery()!!.join()
        }

        assertFalse(sut.busy)
        assertFalse(sut.queryRefiningUnderway)
        assertFalse(sut.queryExecutionUnderway)
        assertEquals(currentResult, sut.resultsViewModel.queryResults)
    }

    @Test
    fun tooManyResults() {
        val resultList = ArrayList<MatchingObject<*>>()
        repeat(SearchResultsListViewModel.MAX_RESULT_SIZE + 5) {
            resultList.add(OriginalMatch(
                Product(
                    17,
                    "A"
                )
            ))
        }

        `when`(queryExecutor.executeQuery(any())).thenReturn(resultList)

        runBlocking {
            sut.refineQuery()!!.join()
            sut.executeQuery()!!.join()
        }

        assertEquals(SearchResultsListViewModel.MAX_RESULT_SIZE + 5, sut.resultsViewModel.actualLastQueryResultSize)
        assertEquals(resultList.subList(0, SearchResultsListViewModel.MAX_RESULT_SIZE), sut.resultsViewModel.queryResults)
    }

    @Test
    fun tooManyResultsReset() {
        tooManyResults()

        assertNotNull(sut.resultsViewModel.actualLastQueryResultSize)

        `when`(queryExecutor.executeQuery(any())).thenReturn(listOf(OriginalMatch(
            Product(
                2,
                "B"
            )
        )))

        runBlocking {
            sut.refineAndExecuteQuery()!!.join()
        }

        assertNull(sut.resultsViewModel.actualLastQueryResultSize)
    }

    @Test
    fun intermediateStates() {
        val refinementStateValid = AtomicBoolean(false)
        val executionStateValid = AtomicBoolean(false)
        `when`(queryRefiner.refine(any())).thenAnswer {
            refinementStateValid.set(
                sut.busy && sut.queryRefiningUnderway
            )
            return@thenAnswer refinedQuery
        }
        `when`(queryExecutor.executeQuery(any())).thenAnswer {
            executionStateValid.set(
                sut.busy && sut.queryExecutionUnderway && !sut.queryRefiningUnderway
            )
        }

        runBlocking {
            sut.refineAndExecuteQuery()!!.join()
        }

        assertTrue(refinementStateValid.get())
        assertTrue(executionStateValid.get())
    }

}