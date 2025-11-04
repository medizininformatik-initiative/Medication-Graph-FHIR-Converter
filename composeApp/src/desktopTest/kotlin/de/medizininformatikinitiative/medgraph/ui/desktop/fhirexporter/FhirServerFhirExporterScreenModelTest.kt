package de.medizininformatikinitiative.medgraph.ui.desktop.fhirexporter

import de.medizininformatikinitiative.medgraph.UnitTest
import de.medizininformatikinitiative.medgraph.fhirexporter.FhirServerExportSink
import de.medizininformatikinitiative.medgraph.fhirexporter.FhirServerExportSinkFactory
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.`when`
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
        sut = FhirServerFhirExporterScreenModel(sinkFactory)
        `when`(sinkFactory.prepareExportWithoutAuth(any())).thenReturn(fhirExport)
        `when`(sinkFactory.prepareExportWithHttpBasicAuth(any(), any(), any())).thenReturn(fhirExport)
        `when`(sinkFactory.prepareExportWithTokenAuth(any(), any())).thenReturn(fhirExport)
        `when`(fhirExport.currentTaskStack).thenReturn(arrayOf())

        sut.fhirBaseUrl = "http://localhost:8080/fhir"
        sut.username = ""
        sut.password = ""
        sut.bearerToken = ""
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
        sut.fhirBaseUrl = " "
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
        sut.username = "admin"
        assertNull(sut.exportBasicAuth())
        assertNull(sut.exportTokenAuth())
        sut.password = "secure"
        assertNotNull(sut.exportBasicAuth())
        assertNull(sut.exportTokenAuth())
        sut.username = ""
        assertNull(sut.exportBasicAuth())
        assertNull(sut.exportTokenAuth())
    }

    @Test
    fun tokenAuthNeedsToken() {
        assertNull(sut.exportTokenAuth())
        sut.bearerToken = "myToken"
        assertNotNull(sut.exportTokenAuth())
    }

}