package de.medizininformatikinitiative.medgraph.ui.desktop.fhirexporter

import de.medizininformatikinitiative.medgraph.TempDirectoryTestExtension
import de.medizininformatikinitiative.medgraph.UnitTest
import de.medizininformatikinitiative.medgraph.common.mvc.Progressable
import de.medizininformatikinitiative.medgraph.fhirexporter.FhirExport
import de.medizininformatikinitiative.medgraph.fhirexporter.FhirExportFactory
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * @author Markus Budeus
 */
@ExtendWith(TempDirectoryTestExtension::class)
class FhirExporterScreenModelTest : UnitTest() {

    @Mock
    lateinit var fhirExporter: FhirExportFactory

    @Mock
    lateinit var fhirExport: FhirExport

    lateinit var sut: FhirExporterScreenModel

    @BeforeEach
    fun setUp(tempDirectory: Path) {
        insertDatabaseConnectionServiceMock()
        sut = FhirExporterScreenModel(fhirExporter)
        `when`(fhirExporter.prepareExport(any())).thenReturn(fhirExport)
        `when`(fhirExport.currentTaskStack).thenReturn(arrayOf())
        sut.exportPath = tempDirectory.toAbsolutePath().toString()
    }

    @Test
    fun initialState() {
        assertNull(sut.errorText)
        assertNull(sut.exportTask.progressable)
        assertFalse(sut.exportUnderway)
    }

    @Test
    fun exportSucceeds() {
        runExportSync()
        assertNull(sut.errorText)
        assertNull(sut.exportTask.progressable)
        assertFalse(sut.exportUnderway)
    }

    @Test
    fun intermediateState() {
        val exportTaskPresent = AtomicBoolean(false)
        doAnswer {
            exportTaskPresent.set(sut.exportTask.progressable != null)
        }.`when`(fhirExport).doExport(any())

        runExportSync()
        assertTrue(exportTaskPresent.get())
    }

    @Test
    fun intermediateStateAfterRestart() {
        runExportSync()

        val recordedExportTask = AtomicReference<Progressable?>(null)
        val recordedExportUnderwayState = AtomicBoolean(false)
        doAnswer {
            recordedExportTask.set(sut.exportTask.progressable)
            recordedExportUnderwayState.set(sut.exportUnderway)
        }.`when`(fhirExport).doExport(any())

        runExportSync()

        assertEquals(fhirExport, recordedExportTask.get())
        assertTrue(recordedExportUnderwayState.get())
        assertFalse(sut.exportUnderway)
    }

    @Test
    fun exportFails() {
        doThrow(IllegalStateException("This test went down the drain")).`when`(fhirExport).doExport(any())
        runExportSync()

        assertEquals("This test went down the drain", sut.errorText)
        assertFalse(sut.exportUnderway)
    }

    @Test
    fun correctPathSupplied() {
        val path = Path.of("path", "to", "greatness")
        sut.exportPath = path.toString()
        runExportSync()

        verify(fhirExporter).prepareExport(eq(path))
    }

    @Test
    fun invalidOutputDirectory(directory: Path) {
        val targetPath = directory.resolve("myGreatPath")
        Files.createFile(targetPath)
        sut.exportPath = targetPath.toAbsolutePath().toString()

        runExportSync()

        assertEquals(StringRes.fhir_exporter_invalid_output_dir, sut.errorText)
    }

    @Test
    fun invalidFileInOutputDirectory(directory: Path) {
        Files.createFile(directory.resolve(FhirExport.MEDICATION_OUT_PATH)) // Occupy file name used for medication export dir
        sut.exportPath = directory.toAbsolutePath().toString()

        runExportSync()

        assertEquals(
            StringRes.get(StringRes.fhir_exporter_output_dir_occupied, FhirExport.MEDICATION_OUT_PATH),
            sut.errorText
        )
    }

    @Test
    fun outputDirDoesNotYetExist(directory: Path) {
        sut.exportPath = directory.resolve("ThisIsNew").toAbsolutePath().toString()

        runExportSync()

        assertNull(sut.errorText)
    }

    private fun runExportSync() {
        runBlocking {
            sut.doExport()?.join()
        }
    }

}