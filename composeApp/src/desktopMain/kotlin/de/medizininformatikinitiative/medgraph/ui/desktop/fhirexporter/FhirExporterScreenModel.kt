package de.medizininformatikinitiative.medgraph.ui.desktop.fhirexporter

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import de.medizininformatikinitiative.medgraph.DI
import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnection
import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnectionService
import de.medizininformatikinitiative.medgraph.common.logging.Level
import de.medizininformatikinitiative.medgraph.common.logging.LogManager
import de.medizininformatikinitiative.medgraph.common.mvc.NamedProgressable
import de.medizininformatikinitiative.medgraph.fhirexporter.FhirExport
import de.medizininformatikinitiative.medgraph.fhirexporter.FhirExportFactory
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.theme.templates.ProgressIndicationViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.nio.file.AccessDeniedException
import java.nio.file.Files
import java.nio.file.Path

/**
 * Screen model for the FHIR export tool UI.
 *
 * @author Markus Budeus
 */
class FhirExporterScreenModel(
    private val fhirExporter: FhirExportFactory = DI.get(FhirExportFactory::class.java)
) : ScreenModel {

    private val logger = LogManager.getLogger(FhirExporterScreenModel::class.java)

    /**
     * The current export path as specified by the user.
     */
    var exportPath by mutableStateOf("")

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
     * Attempts to asynchronously execute the export. Returns a job representing the export or null if the export
     * could not start.
     */
    fun doExport(): Job? {
        if (exportUnderway) return null
        return screenModelScope.launch(Dispatchers.IO) { doExportSync() }
    }

    /**
     * Synchronously executes the export.
     */
    private fun doExportSync() {
        synchronized(this) {
            if (exportUnderway) return
            exportUnderway = true
        }
        try {
            doExportTaskChain()
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
    private fun doExportTaskChain() {
        val path = Path.of(exportPath)
        if (!validateAndPrepareExportPath(path)) return

        val export = fhirExporter.prepareExport(path);
        this.exportTask.bind(export)

        DI.get(DatabaseConnectionService::class.java).createConnection().use {
            it.createSession().use { session ->
                export.doExport(session)
            }
        }
    }

    /**
     * Ensures the export path can be used and creates directories if required.
     * @return true if the export path can be used, false if an issue arised during the check
     */
    private fun validateAndPrepareExportPath(path: Path): Boolean {
        if (Files.exists(path)) {
            if (!Files.isDirectory(path)) {
                errorText = StringRes.fhir_exporter_invalid_output_dir
                return false
            }
        } else {
            Files.createDirectories(path)
        }

        for (outputDir in setOf(
            FhirExport.MEDICATION_OUT_PATH,
            FhirExport.SUBSTANCE_OUT_PATH,
            FhirExport.ORGANIZATION_OUT_PATH
        )) {
            val p = path.resolve(outputDir)
            if (Files.exists(p)) {
                if (!Files.isDirectory(p)) {
                    errorText = StringRes.get(StringRes.fhir_exporter_output_dir_occupied, outputDir)
                    return false
                }
            } else {
                Files.createDirectory(p)
            }
        }
        return true
    }

}