package de.medizininformatikinitiative.medgraph.ui.graphdbpopulator

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnection
import de.medizininformatikinitiative.medgraph.graphdbpopulator.GraphDbPopulator
import de.medizininformatikinitiative.medgraph.graphdbpopulator.loaders.Loader
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.nio.file.Path

/**
 * Screen model for the graph db populator ui.
 *
 * @author Markus Budeus
 */
class GraphDbPopulatorScreenModel(
    private val graphDbPopulator: GraphDbPopulator = GraphDbPopulator()
) : ScreenModel {

    /**
     * The user-designated MMI Pharmindex data directory
     */
    var mmiPharmindexDirectory by mutableStateOf("")

    /**
     * The user-designated Neo4j import directory.
     */
    var neo4jImportDirectory by mutableStateOf("")

    /**
     * The current error message to display.
     */
    var errorMessage by mutableStateOf<String?>(null)
        private set

    /**
     * Whether a population is currently underway.
     */
    var executionUnderway by mutableStateOf(false)
        private set

    /**
     * The current major task running as part of the population task chain.
     */
    var executionMajorStep by mutableStateOf("")
        private set

    /**
     * The current number of the major execution steps. (0-indexed)
     */
    var executionMajorStepIndex by mutableStateOf(0)
        private set

    /**
     * The number of major steps currently planned.
     */
    var executionTotalMajorStepsNumber by mutableStateOf(1)
        private set

    /**
     * The current minor task running as part of the population task chain or null if there currently is no minor task
     * running.
     */
    var executionMinorStep by mutableStateOf<String?>(null)
        private set

    /**
     * Whether this view is in "completed" state.
     */
    var executionComplete by mutableStateOf(false)
        private set

    /**
     * Runs the graph db population if not already underway.
     *
     * @return the job representing the population or null if it could not be started
     */
    fun populate(): Job? {
        if (executionUnderway) return null
        return screenModelScope.launch(Dispatchers.IO) {
            populateSync()
        }
    }

    /**
     * Synchronously the graph db population if not already underway.
     */
    private suspend fun populateSync() {
        synchronized(this) {
            if (executionUnderway) return
            executionUnderway = true
        }
        executionComplete = false
        errorMessage = null
        executionMajorStep = ""
        executionMinorStep = null
        executionMajorStepIndex = 0
        executionTotalMajorStepsNumber = 1

        try {

            runPopulationTaskChain()

        } catch (e: Exception) {
            errorMessage = e.message
            e.printStackTrace()
        } finally {
            executionUnderway = false
            executionMajorStep = ""
            executionMinorStep = null
            executionTotalMajorStepsNumber = 1
        }
    }

    private suspend fun runPopulationTaskChain() {
        executionMajorStep = StringRes.graph_db_populator_preparing
        val mmiPharmindexPath: Path = Path.of(mmiPharmindexDirectory)
        val neo4jImportPath: Path = Path.of(neo4jImportDirectory)

        try {
            graphDbPopulator.copyKnowledgeGraphSourceDataToNeo4jImportDirectory(mmiPharmindexPath, neo4jImportPath)
        } catch (e: IllegalArgumentException) {
            errorMessage = e.message
            return
        }

        DatabaseConnection.createDefault().use {
            it.createSession().use { session ->
                val loaders = graphDbPopulator.prepareLoaders(session)
                executionTotalMajorStepsNumber = loaders.size + 3

                executionMajorStepIndex = 1
                executionMajorStep = StringRes.graph_db_populator_clearing_db
                graphDbPopulator.clearDatabase(session)

                executionMajorStepIndex++
                loaders.forEach(this::runLoader)
            }
        }

        executionMajorStep = StringRes.graph_db_populator_cleaning_up
        graphDbPopulator.removeFilesFromNeo4jImportDir(neo4jImportPath)

        executionMajorStepIndex++
        executionComplete = true
    }

    private fun runLoader(loader: Loader) {
        executionMajorStep = StringRes.get(StringRes.graph_db_populator_running_loader, loader.javaClass.simpleName)
        loader.setOnSubtaskStartedListener {
            executionMinorStep = it
        }
        loader.setOnSubtaskCompletedListener {
            executionMinorStep = null
        }
        loader.execute()
        executionMajorStepIndex++
    }

}
