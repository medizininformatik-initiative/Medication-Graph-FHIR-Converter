package de.medizininformatikinitiative.medgraph.ui.desktop.fhirexporter

import de.medizininformatikinitiative.medgraph.UnitTest
import de.medizininformatikinitiative.medgraph.common.db.Neo4jTransactionMemoryLimitTest
import de.medizininformatikinitiative.medgraph.fhirexporter.FhirServerExportSink
import de.medizininformatikinitiative.medgraph.fhirexporter.FhirServerExportSinkFactory
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.Optional
import kotlin.jvm.Throws

/**
 * @author Markus Budeus
 */
class FhirServerFhirExporterScreenModelTest : UnitTest() {

    @Mock
    lateinit var sinkFactory: FhirServerExportSinkFactory

    @Mock
    lateinit var fhirExport: FhirServerExportSink

    lateinit var sut: FhirServerFhirExporterScreenModel

    @BeforeEach
    fun setUp() {
        insertDatabaseConnectionServiceMock()
        sut = FhirServerFhirExporterScreenModel(sinkFactory, null)
        `when`(sinkFactory.prepareExportWithoutAuth(any())).thenReturn(fhirExport)
        `when`(sinkFactory.prepareExportWithHttpBasicAuth(any(), any(), any())).thenReturn(fhirExport)
        `when`(sinkFactory.prepareExportWithTokenAuth(any(), any())).thenReturn(fhirExport)
        `when`(fhirExport.currentTaskStack).thenReturn(arrayOf())

        sut.fhirBaseUrl.value = "http://localhost:8080/fhir"
        sut.username.value = ""
        sut.password.value = ""
        sut.bearerToken.value = ""
    }

    @Test
    fun initialState() {
        assertNull(sut.errorText)
        assertNull(sut.exportTask.progressable)
        assertFalse(sut.exportUnderway)
    }

    @Test
    fun exportSucceeds() {
        runBlocking {
            sut.exportNoAuth()?.join()
        }
        assertNull(sut.errorText)
        assertNull(sut.exportTask.progressable)
        assertFalse(sut.exportUnderway)
    }

    @Test
    fun alwaysRequiresUrl() {
        sut.fhirBaseUrl.value = " "
        assertNull(sut.exportNoAuth())
        assertNull(sut.exportBasicAuth())
        assertNull(sut.exportTokenAuth())
    }

    @Test
    fun noAuthRequiresNothingElse() {
        assertNotNull(sut.exportNoAuth())
        assertNull(sut.exportBasicAuth())
        assertNull(sut.exportTokenAuth())
    }

    @Test
    fun httpBasicAuthNeedsUserAndPassword() {
        assertNull(sut.exportBasicAuth())
        assertNull(sut.exportTokenAuth())
        sut.username.value = "admin"
        assertNull(sut.exportBasicAuth())
        assertNull(sut.exportTokenAuth())
        sut.password.value = "secure"
        assertNotNull(sut.exportBasicAuth())
        assertNull(sut.exportTokenAuth())
        sut.username.value = ""
        assertNull(sut.exportBasicAuth())
        assertNull(sut.exportTokenAuth())
    }

    @Test
    fun tokenAuthNeedsToken() {
        assertNull(sut.exportTokenAuth())
        sut.bearerToken.value = "myToken"
        assertNotNull(sut.exportTokenAuth())
    }

    @Test
    fun memoryLimitFine() {
        val limitTester = mock(Neo4jTransactionMemoryLimitTest::class.java)
        `when`(limitTester.probeNeo4jTransactionSizeLimit(any())).thenReturn(Optional.empty())
        sut = FhirServerFhirExporterScreenModel(sinkFactory, limitTester)
        assertNull(sut.warningText)
    }

    @Test
    fun memoryLimitProblematic() {
        val limitTester = mock(Neo4jTransactionMemoryLimitTest::class.java)
        `when`(limitTester.probeNeo4jTransactionSizeLimit(any())).thenReturn(Optional.of("Your memory limit is trash. Fix it."))
        sut = FhirServerFhirExporterScreenModel(sinkFactory, limitTester)
        assertTrue(sut.warningText?.contains("Your memory limit is trash. Fix it.") ?: false)
    }

}