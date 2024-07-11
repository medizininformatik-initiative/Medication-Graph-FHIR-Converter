package de.medizininformatikinitiative.medgraph.ui.desktop.graphdbpopulator

import de.medizininformatikinitiative.medgraph.graphdbpopulator.GraphDbPopulation
import de.medizininformatikinitiative.medgraph.graphdbpopulator.GraphDbPopulator
import de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders.Loader
import de.medizininformatikinitiative.medgraph.ui.UnitTest
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer

/**
 * @author Markus Budeus
 */
class GraphDbPopulatorScreenModelTest : UnitTest() {

    @Mock
    private lateinit var graphDbPopulator: GraphDbPopulator

    @Mock
    private lateinit var graphDbPopulation: GraphDbPopulation

    private lateinit var sut: GraphDbPopulatorScreenModel

    @BeforeEach
    fun setUp() {
        `when`(graphDbPopulator.prepareDatabasePopulation(any(), any(), any())).thenReturn(graphDbPopulation)
        sut = GraphDbPopulatorScreenModel(graphDbPopulator)

        sut.mmiPharmindexDirectory = System.getProperty("user.home")
        sut.neo4jImportDirectory = System.getProperty("user.home")
    }

    @Test
    fun initialState() {
        assertNull(sut.errorMessage)
        assertFalse(sut.executionComplete)
        assertFalse(sut.executionUnderway)
        assertNull(sut.executionTask)
    }

    @Test
    fun executionCompleteState() {
        runSut()
        assertFalse(sut.executionUnderway)
        assertTrue(sut.executionComplete)
        assertNull(sut.errorMessage)
        assertEquals(graphDbPopulation, sut.executionTask)
    }

    @Test
    fun restartingExecution() {
        runSut()

        val otherTask: GraphDbPopulation = mock()
        `when`(graphDbPopulator.prepareDatabasePopulation(any(), any(), any())).thenReturn(otherTask)
        val completedStateGoneAgain = AtomicBoolean(false)
        val newTaskAssigned = AtomicBoolean(false)

        doAnswer({ req ->
            completedStateGoneAgain.set(!sut.executionComplete)
            newTaskAssigned.set(sut.executionTask == otherTask)
            return@doAnswer null
        }).`when`(otherTask).executeDatabasePopulation(any())

        runSut()

        assertTrue(completedStateGoneAgain.get())
        assertTrue(newTaskAssigned.get())
    }

    @Test
    fun executionFails() {
        doThrow(RuntimeException("This went terribly wrong!")).`when`(graphDbPopulation).executeDatabasePopulation(any())

        runSut()

        assertFalse(sut.executionComplete)
        assertEquals("This went terribly wrong!", sut.errorMessage)
    }

    @Test
    fun preparationFails() {
        doThrow(IllegalArgumentException("No way!")).`when`(graphDbPopulator)
            .prepareDatabasePopulation(any(), any(), any())

        runSut()

        assertFalse(sut.executionComplete)
        assertEquals("No way!", sut.errorMessage)
    }

    @Test
    fun withAmicePath() {
        sut.amiceStoffBezFile = "/boot/efi"
        runSut()

        verify(graphDbPopulator).prepareDatabasePopulation(
            Path.of(sut.mmiPharmindexDirectory),
            Path.of(sut.neo4jImportDirectory),
            Path.of("/boot/efi"),
        )
    }

    @Test
    fun withoutAmicePath() {
        sut.amiceStoffBezFile = ""
        runSut()

        verify(graphDbPopulator).prepareDatabasePopulation(
            Path.of(sut.mmiPharmindexDirectory),
            Path.of(sut.neo4jImportDirectory),
            null,
        )
    }

    private fun runSut() {
        runBlocking {
            sut.populate()?.join()
        }
    }

}