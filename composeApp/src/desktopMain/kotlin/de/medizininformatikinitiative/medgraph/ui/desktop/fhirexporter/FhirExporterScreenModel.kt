package de.medizininformatikinitiative.medgraph.ui.desktop.fhirexporter

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import de.medizininformatikinitiative.medgraph.DI
import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnectionService
import de.medizininformatikinitiative.medgraph.common.db.Neo4jTransactionMemoryLimitTest
import de.medizininformatikinitiative.medgraph.common.logging.Level
import de.medizininformatikinitiative.medgraph.common.logging.LogManager
import de.medizininformatikinitiative.medgraph.fhirexporter.FhirExportSink
import de.medizininformatikinitiative.medgraph.fhirexporter.FileFhirExportSink
import de.medizininformatikinitiative.medgraph.fhirexporter.FhirExportSources
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.theme.templates.ProgressIndicationViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.nio.file.AccessDeniedException
import java.nio.file.Files
import java.nio.file.Path

/**
 * Screen model for the FHIR export tool UI. This is a generic implementation which is independent of the
 * [FhirExportSink]-implementation used.
 *
 * @param neo4jTransactionMemoryLimitTest The [Neo4jTransactionMemoryLimitTest] instance to use for probing the
 * Neo4j transaction limits and showing a warning if they are insufficient. If null, no such test is performed.
 * @author Markus Budeus
 */
abstract class FhirExporterScreenModel(
    neo4jTransactionMemoryLimitTest: Neo4jTransactionMemoryLimitTest? = DI.get(Neo4jTransactionMemoryLimitTest::class.java)
) : ScreenModel {

    private val logger = LogManager.getLogger(FhirExporterScreenModel::class.java)

    /**
     * Whether an export is currently underway.
     */
    var exportUnderway by mutableStateOf(false)

    /**
     * View state for the current export task.
     */
    var exportTask = ProgressIndicationViewState()

    /**
     * In case there was an error, information about the last occurred error. Otherwise, null.
     */
    var errorText by mutableStateOf<String?>(null)

    /**
     * An optional warning text to display.
     */
    var warningText by mutableStateOf<String?>(null)

    init {
        if (neo4jTransactionMemoryLimitTest != null) {
            DI.get(DatabaseConnectionService::class.java).createConnection().use {
                it.createSession().use { session ->
                    val memoryLimitWarning = neo4jTransactionMemoryLimitTest.probeNeo4jTransactionSizeLimit(session)
                    if (memoryLimitWarning.isPresent) {
                        this.warningText = memoryLimitWarning.get()
                    }
                }
            }
        }
    }

    /**
     * Attempts to asynchronously execute the export. Returns a job representing the export or null if the export
     * could not start.
     *
     * @param sink The [FhirExportSink] to export into.
     */
    fun doExport(sink: FhirExportSink): Job? {
        if (exportUnderway) return null
        return screenModelScope.launch(Dispatchers.IO) { doExportSync(sink) }
    }

    /**
     * Synchronously executes the export.
     */
    private fun doExportSync(sink: FhirExportSink) {
        synchronized(this) {
            if (exportUnderway) return
            exportUnderway = true
        }
        try {
            doExportTaskChain(sink)
        } catch (e: AccessDeniedException) {
            logger.log(Level.WARN, "Missing permissions for export.", e)
            errorText = StringRes.fhir_exporter_missing_permissions

        } catch (e: Exception) {
            logger.log(Level.ERROR, "FHIR Export failed.", e)
            errorText = e.message
        } finally {
            exportUnderway = false
            exportTask.unbind()
        }
    }

    /**
     * Does the tasks required for the export, however not including exception handling.
     */
    private fun doExportTaskChain(sink: FhirExportSink) {
        this.exportTask.bind(sink)

        DI.get(DatabaseConnectionService::class.java).createConnection().use {
            it.createSession().use { session ->
                sink.doExport(FhirExportSources.forNeo4jSession(session))
            }
        }
    }

}