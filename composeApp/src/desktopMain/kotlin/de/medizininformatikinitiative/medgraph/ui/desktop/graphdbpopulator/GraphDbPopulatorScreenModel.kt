package de.medizininformatikinitiative.medgraph.ui.desktop.graphdbpopulator

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnection
import de.medizininformatikinitiative.medgraph.common.logging.Level
import de.medizininformatikinitiative.medgraph.common.logging.LogManager
import de.medizininformatikinitiative.medgraph.common.mvc.NamedProgressable
import de.medizininformatikinitiative.medgraph.graphdbpopulator.GraphDbPopulation
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

    private val logger = LogManager.getLogger(GraphDbPopulatorScreenModel::class.java)

    /**
     * The user-designated MMI Pharmindex data directory.
     */
    var mmiPharmindexDirectory by mutableStateOf("")

    /**
     * The user-designated Neo4j import directory.
     */
    var neo4jImportDirectory by mutableStateOf("")

    /**
     * The user-designated path to the "AMIce Stoffbezeichnungen Rohdaten" file. Empty means it is not to be used.
     */
    var amiceStoffBezFile by mutableStateOf("")

    /**
     * The current error message to display.
     */
    var errorMessage by mutableStateOf<String?>(null)

    /**
     * Whether a population is currently underway.
     */
    var executionUnderway by mutableStateOf(false)

    /**
     * The currently ongoing population task or null if no population is ongoing.
     */
    var executionTask by mutableStateOf<NamedProgressable?>(null)

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
    private fun populateSync() {
        synchronized(this) {
            if (executionUnderway) return
            executionUnderway = true
        }
        executionComplete = false
        errorMessage = null

        try {

            runPopulationTaskChain()

        } catch (e: Exception) {
            logger.log(Level.ERROR, "Graph DB population failed.", e)
            errorMessage = e.message
        } finally {
            executionUnderway = false
        }
    }

    private fun runPopulationTaskChain() {
        val mmiPharmindexPath: Path = Path.of(mmiPharmindexDirectory)
        val neo4jImportPath: Path = Path.of(neo4jImportDirectory)
        val amiceFilePath: Path? = if (amiceStoffBezFile.isEmpty()) null else Path.of(amiceStoffBezFile)

        val population = graphDbPopulator.prepareDatabasePopulation(mmiPharmindexPath, neo4jImportPath, amiceFilePath)
        this.executionTask = population

        try {
            population.executeDatabasePopulation(DatabaseConnection.createDefault());
        } catch (e: IllegalArgumentException) {
            errorMessage = e.message
            return
        }
        executionComplete = true
    }

}
