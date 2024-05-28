package de.medizininformatikinitiative.medgraph.ui.desktop.fhirexporter

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnection
import de.medizininformatikinitiative.medgraph.fhirexporter.FhirExporter
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.nio.file.Files
import java.nio.file.Path

/**
 * Screen model for the FHIR export tool UI.
 *
 * @author Markus Budeus
 */
class FhirExporterScreenModel(
    private val fhirExporter: FhirExporter = FhirExporter()
) : ScreenModel {

    /**
     * The current export path as specified by the user.
     */
    var exportPath by mutableStateOf("")

    /**
     * Whether an export is currently underway.
     */
    var exportUnderway by mutableStateOf(false)

    /**
     * Current export progress as a numeric value. (Relative to [exportMaxProgress])
     */
    var exportProgress by mutableStateOf(0)

    /**
     * Human-readable description of the current export task.
     */
    var exportCurrentTask by mutableStateOf("")

    /**
     * The maximum export progress, i.e. when the export is complete.
     */
    val exportMaxProgress = 3

    /**
     * In case there was an error, information about the last occurred error. Otherwise null.
     */
    var errorText by mutableStateOf<String?>(null)

    /**
     * Attempts to asynchronously execute the export. Returns a job representing the export or null if the export
     * could not start.
     */
    fun doExport(): Job? {
        if (exportUnderway) return null
        return screenModelScope.launch { doExportSync() }
    }

    /**
     * Synchronously executes the export.
     */
    private fun doExportSync() {
        synchronized(this) {
            if (exportUnderway) return
            exportUnderway = true
        }
        exportProgress = 0
        try {
            doExportTaskChain()
        } catch (e: Exception) {
            errorText = e.message
        } finally {
            exportUnderway = false
        }
    }

    /**
     * Does the tasks required for the export, however not including exception handling.
     */
    private fun doExportTaskChain() {
        val path = Path.of(exportPath)
        if (!validateAndPrepareExportPath(path)) return
        DatabaseConnection.createDefault().use {
            it.createSession().use { session ->
                exportCurrentTask = StringRes.fhir_exporter_exporting_medications
                fhirExporter.exportMedications(session, path.resolve(FhirExporter.MEDICATION_OUT_PATH), false)
                exportProgress = 1
                exportCurrentTask = StringRes.fhir_exporter_exporting_substances
                fhirExporter.exportSubstances(session, path.resolve(FhirExporter.SUBSTANCE_OUT_PATH), false)
                exportProgress = 2
                exportCurrentTask = StringRes.fhir_exporter_exporting_organizations
                fhirExporter.exportOrganizations(session, path.resolve(FhirExporter.ORGANIZATION_OUT_PATH))
                exportProgress = 3
                exportCurrentTask = ""
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
            FhirExporter.MEDICATION_OUT_PATH,
            FhirExporter.SUBSTANCE_OUT_PATH,
            FhirExporter.ORGANIZATION_OUT_PATH
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