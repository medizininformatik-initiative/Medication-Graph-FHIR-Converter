package de.medizininformatikinitiative.medgraph.ui.graphdbpopulator

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
    private lateinit var loader1: Loader

    @Mock
    private lateinit var loader2: Loader

    @Mock
    private lateinit var loader3: Loader

    private lateinit var sut: GraphDbPopulatorScreenModel

    @BeforeEach
    fun setUp() {
        sut = GraphDbPopulatorScreenModel(graphDbPopulator)

        sut.mmiPharmindexDirectory = System.getProperty("user.home")
        sut.neo4jImportDirectory = System.getProperty("user.home")

        `when`(graphDbPopulator.prepareLoaders(any())).thenReturn(listOf(loader1, loader2, loader3))
    }

    @Test
    fun initialState() {
        assertNull(sut.errorMessage)
        assertFalse(sut.executionComplete)
        assertFalse(sut.executionUnderway)
        assertNull(sut.executionMinorStep)
    }

    @Test
    fun executionCompleteState() {
        runSut()
        assertFalse(sut.executionUnderway)
        assertTrue(sut.executionComplete)
        assertNull(sut.errorMessage)
    }

    @Test
    fun databaseIsClearedUponExecution() {
        runSut()
        verify(graphDbPopulator).clearDatabase(any())
    }

    @Test
    fun cleanupHappensDuringExecution() {
        runSut()
        verify(graphDbPopulator).removeFilesFromNeo4jImportDir(Path.of(sut.neo4jImportDirectory))
    }

    @Test
    fun intermediateStates() {

    }

    @Test
    fun restartingExecution() {
        runSut()

        val completedStateGoneAgain = AtomicBoolean(false)
        val intermediateProgressAgain = AtomicBoolean(false)

        doAnswer({ req ->
            completedStateGoneAgain.set(!sut.executionComplete)
            intermediateProgressAgain.set(sut.executionMajorStepIndex < sut.executionTotalMajorStepsNumber)
            return@doAnswer null
        }).`when`(loader2).execute()

        runSut()

        assertTrue(completedStateGoneAgain.get())
        assertTrue(intermediateProgressAgain.get())
    }

    @Test
    fun loaderFails() {
        doThrow(RuntimeException("This went terribly wrong!")).`when`(loader3).execute()

        runSut()

        assertFalse(sut.executionComplete)
        assertEquals("This went terribly wrong!", sut.errorMessage)
    }

    @Test
    fun preparationFails() {
        doThrow(IllegalArgumentException("No way!")).`when`(graphDbPopulator)
            .copyKnowledgeGraphSourceDataToNeo4jImportDirectory(
                Path.of(sut.mmiPharmindexDirectory),
                Path.of(sut.neo4jImportDirectory)
            )

        runSut()

        assertFalse(sut.executionComplete)
        assertEquals("No way!", sut.errorMessage)
        verify(loader1, never()).execute()
    }

    @Test
    fun minorStep() {
        val subtaskStartListener = AtomicReference<Consumer<String>>()
        val subtaskEndListener = AtomicReference<Runnable>()

        doAnswer {
            subtaskStartListener.set(it.getArgument(0))
        }.`when`(loader3).setOnSubtaskStartedListener(any())
        doAnswer {
            subtaskEndListener.set(it.getArgument(0))
        }.`when`(loader3).setOnSubtaskCompletedListener(any())

        val subtaskNameSet = AtomicBoolean(false)
        val subtaskNameCleared = AtomicBoolean(false)

        doAnswer {
            subtaskStartListener.get().accept("Subtask A113")
            subtaskNameSet.set(sut.executionMinorStep == "Subtask A113")
            subtaskEndListener.get().run()
            subtaskNameCleared.set(sut.executionMinorStep == null)
        }.`when`(loader3).execute()

        runSut()

        assertTrue(subtaskNameSet.get())
        assertTrue(subtaskNameCleared.get())
    }

    private fun runSut() {
        runBlocking {
            sut.populate()?.join()
        }
    }

}