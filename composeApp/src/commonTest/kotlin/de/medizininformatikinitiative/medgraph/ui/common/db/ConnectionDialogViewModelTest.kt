package de.medizininformatikinitiative.medgraph.ui.common.db

import de.medizininformatikinitiative.medgraph.common.db.ConnectionConfiguration
import de.medizininformatikinitiative.medgraph.common.db.ConnectionConfiguration.ConnectionResult
import de.medizininformatikinitiative.medgraph.common.db.ConnectionPreferences
import de.medizininformatikinitiative.medgraph.ui.UnitTest
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
    lateinit var preferences: ConnectionPreferences

    @Mock
    lateinit var configuration: ConnectionConfiguration

    lateinit var sut: ConnectionDialogViewModel

    @BeforeEach
    fun setUp() {
        `when`(configuration.uri).thenReturn(URI)
        `when`(configuration.user).thenReturn(USER)
        `when`(configuration.hasConfiguredPassword()).thenReturn(true)

        setupSut()
    }

    private fun setupSut() {
        sut = ConnectionDialogViewModel(
            configuration,
            preferences
        ) { configuration.testConnection() }
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

    @ParameterizedTest
    @ValueSource(booleans = booleanArrayOf(false, true))
    fun apply(savePassword: Boolean) {
        `when`(configuration.testConnection()).thenReturn(ConnectionResult.SUCCESS)
        sut.uri.value = "bolt://neo4j"
        sut.user.value = "Neo5k"
        sut.setPassword("Password!")
        sut.savePassword.value = savePassword

        assertTrue(sut.apply().get())

        verify(preferences).user = "Neo5k"
        verify(preferences).connectionUri = "bolt://neo4j"

        if (savePassword) {
            verify(preferences).setPassword("Password!".toCharArray())
        } else {
            verify(preferences).clearPassword()
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = booleanArrayOf(false, true))
    fun applyWithUnchangedPassword(savePassword: Boolean) {
        `when`(configuration.testConnection()).thenReturn(ConnectionResult.SUCCESS)
        sut.savePassword.value = savePassword

        assertTrue(sut.apply().get())

        if (savePassword) {
            verify(preferences, never()).setPassword(any()) // Password unchanged, so we cannot overwrite it!
        } else {
            verify(preferences).clearPassword() // However, we do need to clear it if the user desires so
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = booleanArrayOf(false, true))
    fun applyFails(savePassword: Boolean) {
        `when`(configuration.testConnection()).thenReturn(ConnectionResult.AUTHENTICATION_FAILED)
        sut.savePassword.value = savePassword

        assertFalse(sut.apply().get())

        verify(preferences, never()).connectionUri = any()
        verify(preferences, never()).user = any()
        verify(preferences, never()).setPassword(any())
        verify(preferences, never()).clearPassword()
    }

    @ParameterizedTest
    @EnumSource
    fun testConnection(result: ConnectionResult) {
        `when`(configuration.testConnection()).thenReturn(result)

        assertEquals(result == ConnectionResult.SUCCESS, sut.testConnection().get())
        assertEquals(result, sut.connectionTestResult.value)
    }

    @Test
    fun testConnectionFails() {
        val exception = IllegalStateException("This is a test, so you fail.")
        `when`(configuration.testConnection()).thenThrow(exception)

        try {
            sut.testConnection().get()
            fail("The previous call should have thrown an exception!")
        } catch (e: ExecutionException) {
            assertEquals(exception, e.cause)
        }
    }
}