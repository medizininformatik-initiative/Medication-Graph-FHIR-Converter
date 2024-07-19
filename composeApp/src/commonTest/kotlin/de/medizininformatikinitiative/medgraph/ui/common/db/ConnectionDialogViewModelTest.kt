package de.medizininformatikinitiative.medgraph.ui.common.db

import de.medizininformatikinitiative.medgraph.UnitTest
import de.medizininformatikinitiative.medgraph.common.db.ConnectionConfiguration
import de.medizininformatikinitiative.medgraph.common.db.ConnectionConfigurationService
import de.medizininformatikinitiative.medgraph.common.db.ConnectionFailureReason
import de.medizininformatikinitiative.medgraph.common.db.ConnectionTestService
import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnectionException
import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnectionService
import de.medizininformatikinitiative.medgraph.ui.common.db.ConnectionDialogViewModel.ConnectionResult
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mock
import org.mockito.Mockito.*
import java.util.concurrent.ExecutionException

/**
 * @author Markus Budeus
 */
class ConnectionDialogViewModelTest : UnitTest() {

    private val USER = "user"
    private val URI = "neo4j://FAKEURI.com"

    @Mock
    lateinit var configurationService: ConnectionConfigurationService

    @Mock
    lateinit var testService: ConnectionTestService

    @Mock
    lateinit var configuration: ConnectionConfiguration

    lateinit var sut: ConnectionDialogViewModel

    @BeforeEach
    fun setUp() {
        `when`(configuration.uri).thenReturn(URI)
        `when`(configuration.user).thenReturn(USER)
        `when`(configuration.hasConfiguredPassword()).thenReturn(true)
        `when`(configurationService.connectionConfiguration).thenReturn(configuration)

        insertMockDependency(ConnectionConfigurationService::class.java, configurationService)
        insertMockDependency(ConnectionTestService::class.java, testService)

        setupSut()
    }

    private fun setupSut() {
        sut = ConnectionDialogViewModel(configurationService, testService)
    }

    @Test
    fun initialUri() {
        assertEquals(URI, sut.uri.value)
    }

    @Test
    fun initialUser() {
        assertEquals(USER, sut.user.value)
    }

    @Test
    fun getPasswordUnchanged() {
        assertTrue(sut.configuredPasswordExists)
        assertTrue(sut.passwordUnchanged.value)
        sut.setPassword("")
        assertFalse(sut.passwordUnchanged.value)
    }

    @Test
    fun getPasswordUnchangedWithoutPreconfiguredPassword() {
        `when`(configuration.hasConfiguredPassword()).thenReturn(false)
        setupSut()

        assertFalse(sut.configuredPasswordExists)
        assertFalse(sut.passwordUnchanged.value)
        sut.setPassword("AAA")
        assertFalse(sut.passwordUnchanged.value)
    }

    @Test
    fun setPassword() {
        sut.setPassword("Hello world!")
        assertEquals("Hello world!", sut.password.value)
        sut.setPassword("")
        assertEquals("", sut.password.value)
    }

    @ParameterizedTest(name = "savePassword: {0}")
    @ValueSource(booleans = booleanArrayOf(false, true))
    fun apply(savePassword: Boolean) {
        sut.uri.value = "bolt://neo4j"
        sut.user.value = "Neo5k"
        sut.setPassword("Password!")
        sut.savePassword.value = savePassword

        assertTrue(sut.apply().get())

        val expectedConfig = ConnectionConfiguration("bolt://neo4j", "Neo5k", "Password!".toCharArray());
        if (savePassword) {
            verify(configurationService).setConnectionConfiguration(
                eq(expectedConfig),
                eq(ConnectionConfigurationService.SaveOption.SAVE_ALL)
            )
        } else {
            verify(configurationService).setConnectionConfiguration(
                eq(expectedConfig),
                eq(ConnectionConfigurationService.SaveOption.EXCLUDE_PASSWORD)
            )
        }
    }

    @ParameterizedTest(name = "savePassword: {0}")
    @ValueSource(booleans = booleanArrayOf(false, true))
    fun applyWithUnchangedPassword(savePassword: Boolean) {
        sut.savePassword.value = savePassword

        assertTrue(sut.apply().get())

        val expectedConfig = ConnectionConfiguration(URI, USER, configuration);
        if (savePassword) {
            verify(configurationService).setConnectionConfiguration(
                eq(expectedConfig),
                eq(ConnectionConfigurationService.SaveOption.SAVE_ALL)
            )
        } else {
            verify(configurationService).setConnectionConfiguration(
                eq(expectedConfig),
                eq(ConnectionConfigurationService.SaveOption.EXCLUDE_PASSWORD)
            )
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = booleanArrayOf(false, true))
    fun applyFails(savePassword: Boolean) {
        `when`(testService.verifyConnection(notNull())).thenThrow(
            DatabaseConnectionException(
                ConnectionFailureReason.AUTHENTICATION_FAILED,
                "Invalid authentication provided."
            )
        )
        sut.savePassword.value = savePassword

        assertFalse(sut.apply().get())

        verify(configurationService, never()).setConnectionConfiguration(any(), any())
    }

    @ParameterizedTest
    @EnumSource
    fun testConnection(result: ConnectionFailureReason) {
        `when`(testService.verifyConnection(notNull())).thenThrow(
            DatabaseConnectionException(result, "Something went wrong.")
        )

        assertFalse(sut.testConnection().get())

        val expected = when(result) {
            ConnectionFailureReason.INVALID_CONNECTION_STRING -> ConnectionResult.INVALID_CONNECTION_STRING
            ConnectionFailureReason.SERVICE_UNAVAILABLE -> ConnectionResult.SERVICE_UNAVAILABLE
            ConnectionFailureReason.AUTHENTICATION_FAILED -> ConnectionResult.AUTHENTICATION_FAILED
            ConnectionFailureReason.INTERNAL_ERROR -> ConnectionResult.INTERNAL_ERROR
        }

        assertEquals(expected, sut.connectionTestResult.value)
    }

    @Test
    fun testConnectionFails() {
        val exception = IllegalStateException("This is a test, so you fail.")
        `when`(testService.verifyConnection(notNull())).thenThrow(exception)

        try {
            sut.testConnection().get()
            fail("The previous call should have thrown an exception!")
        } catch (e: ExecutionException) {
            assertEquals(exception, e.cause)
        }
    }
}