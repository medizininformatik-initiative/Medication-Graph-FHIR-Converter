package de.medizininformatikinitiative.medgraph.ui.desktop.fhirexporter

import de.medizininformatikinitiative.medgraph.fhirexporter.FhirExporter
import de.medizininformatikinitiative.medgraph.ui.TempDirectoryTestExtension
import de.medizininformatikinitiative.medgraph.ui.UnitTest
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author Markus Budeus
 */
@ExtendWith(TempDirectoryTestExtension::class)
class FhirExporterScreenModelTest : UnitTest() {

    @Mock
    lateinit var fhirExporter: FhirExporter

    lateinit var sut: FhirExporterScreenModel

    @BeforeEach
    fun setUp(tempDirectory: Path) {
        sut = FhirExporterScreenModel(fhirExporter)
        sut.exportPath = tempDirectory.toAbsolutePath().toString()
    }

    @Test
    fun initialState() {
        assertNull(sut.errorText)
        assertEquals(0, sut.exportProgress)
        assertFalse(sut.exportUnderway)
    }

    @Test
    fun exportSucceeds() {
        runExportSync()
        assertEquals(sut.exportMaxProgress, sut.exportProgress)
        assertNull(sut.errorText)
        assertFalse(sut.exportUnderway)
    }

    @Test
    fun intermediateState() {
        val exportAlwaysUnderway = AtomicBoolean(true)
        val recordedProgressStates = HashSet<Int>()
        doAnswer {
            recordedProgressStates.add(sut.exportProgress)
            exportAlwaysUnderway.set(exportAlwaysUnderway.get() && sut.exportUnderway)
        }.`when`(fhirExporter).exportMedications(any(), any(), anyBoolean())
        doAnswer {
            recordedProgressStates.add(sut.exportProgress)
            exportAlwaysUnderway.set(exportAlwaysUnderway.get() && sut.exportUnderway)
        }.`when`(fhirExporter).exportSubstances(any(), any(), anyBoolean())
        doAnswer {
            recordedProgressStates.add(sut.exportProgress)
            exportAlwaysUnderway.set(exportAlwaysUnderway.get() && sut.exportUnderway)
        }.`when`(fhirExporter).exportOrganizations(any(), any())

        runExportSync()

        assertEquals(setOf(0, 1, 2), recordedProgressStates)
        assertTrue(exportAlwaysUnderway.get())
    }

    @Test
    fun intermediateStateAfterRestart() {
        runExportSync()

        val recordedProgressState = AtomicInteger(-1)
        val recordedExportUnderwayState = AtomicBoolean(false)
        doAnswer {
            recordedProgressState.set(sut.exportProgress)
            recordedExportUnderwayState.set(sut.exportUnderway)
        }.`when`(fhirExporter).exportOrganizations(any(), any())

        runExportSync()

        assertNotEquals(sut.exportMaxProgress, recordedProgressState.get())
        assertNotEquals(-1, recordedProgressState.get())
        assertTrue(recordedExportUnderwayState.get())
        assertFalse(sut.exportUnderway)
    }

    @Test
    fun exportFails() {
        doThrow(IllegalStateException("This test went down the drain")).`when`(fhirExporter)
            .exportMedications(any(), any(), anyBoolean())
        runExportSync()

        assertEquals("This test went down the drain", sut.errorText)
        assertFalse(sut.exportUnderway)
    }

    @Test
    fun correctPathSupplied() {
        val path = Path.of("path", "to", "greatness")
        sut.exportPath = path.toString()
        runExportSync()

        verify(fhirExporter).exportMedications(any(), eq(path.resolve(FhirExporter.MEDICATION_OUT_PATH)), anyBoolean())
        verify(fhirExporter).exportSubstances(any(), eq(path.resolve(FhirExporter.SUBSTANCE_OUT_PATH)), anyBoolean())
        verify(fhirExporter).exportOrganizations(any(), eq(path.resolve(FhirExporter.ORGANIZATION_OUT_PATH)))
    }

    @Test
    fun invalidOutputDirectory(directory: Path) {
        val targetPath = directory.resolve("myGreatPath")
        Files.createFile(targetPath)
        sut.exportPath = targetPath.toAbsolutePath().toString()

        runExportSync()

        assertEquals(StringRes.fhir_exporter_invalid_output_dir, sut.errorText)
        assertTrue(sut.exportProgress < sut.exportMaxProgress)
    }

    @Test
    fun invalidFileInOutputDirectory(directory: Path) {
        Files.createFile(directory.resolve(FhirExporter.MEDICATION_OUT_PATH)) // Occupy file name used for medication export dir
        sut.exportPath = directory.toAbsolutePath().toString()

        runExportSync()

        assertEquals(
            StringRes.get(StringRes.fhir_exporter_output_dir_occupied, FhirExporter.MEDICATION_OUT_PATH),
            sut.errorText
        )
        assertTrue(sut.exportProgress < sut.exportMaxProgress)
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