package de.medizininformatikinitiative.medgraph.ui.desktop.fhirexporter

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.medizininformatikinitiative.medgraph.DI
import de.medizininformatikinitiative.medgraph.common.db.Neo4jTransactionMemoryLimitTest
import de.medizininformatikinitiative.medgraph.fhirexporter.FileFhirExportSinkFactory
import de.medizininformatikinitiative.medgraph.fhirexporter.FileFhirExportSink
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import kotlinx.coroutines.Job
import java.nio.file.Files
import java.nio.file.Path

/**
 * [FhirExporterScreenModel]-implementation used when exporting to JSON files.
 *
 * @author Markus Budeus
 */
class FileFhirExporterScreenModel(
    private val fhirExporter: FileFhirExportSinkFactory = DI.get(FileFhirExportSinkFactory::class.java),
    neo4jTransactionMemoryLimitTest: Neo4jTransactionMemoryLimitTest? = DI.get(Neo4jTransactionMemoryLimitTest::class.java)
) : FhirExporterScreenModel(neo4jTransactionMemoryLimitTest) {

    /**
     * The current export path as specified by the user.
     */
    var exportPath by mutableStateOf("")

    fun doExport(): Job? {
        val path = Path.of(exportPath)
        if (!validateAndPrepareExportPath(path)) return null
        return super.doExport(fhirExporter.prepareExport(path))
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
            FileFhirExportSink.MEDICATION_OUT_PATH,
            FileFhirExportSink.SUBSTANCE_OUT_PATH,
            FileFhirExportSink.ORGANIZATION_OUT_PATH
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