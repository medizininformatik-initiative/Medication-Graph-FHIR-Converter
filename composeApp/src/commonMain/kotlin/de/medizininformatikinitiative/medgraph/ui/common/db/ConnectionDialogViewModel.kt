package de.medizininformatikinitiative.medgraph.ui.common.db

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import de.medizininformatikinitiative.medgraph.common.db.ConnectionConfiguration
import de.medizininformatikinitiative.medgraph.common.db.ConnectionConfiguration.*
import de.medizininformatikinitiative.medgraph.common.db.ConnectionPreferences
import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import medicationgraphfhirconverter.composeapp.generated.resources.Res
import medicationgraphfhirconverter.composeapp.generated.resources.db_connection_dialog_password_unchanged
import org.jetbrains.compose.resources.getString
import java.net.URISyntaxException

/**
 * Manages the state of the database connection dialog.
 *
 * @author Markus Budeus
 */
class ConnectionDialogViewModel(
    private val preferences: ConnectionPreferences,
    private val coroutineScope: CoroutineScope,
    private val onFinish: () -> Unit = {}
) {

    val uri: MutableState<String>
    val user: MutableState<String>
    val password: State<String>
        get() = passwordInternal
    private val passwordInternal: MutableState<String>

    /**
     * Whether the password is to be updated or the currently configured password is to be used.
     */
    val passwordUnchanged: MutableState<Boolean>

    /**
     * In case the password is updated, whether it shall be saved.
     */
    val savePassword = mutableStateOf(true)

    /**
     * Whether a configured password exists, which is requried to use to [passwordUnchanged] properly.
     */
    val configuredPasswordExists: Boolean

    /**
     * Whether the connection is currently being tested.
     */
    val testingConnection = mutableStateOf(false)

    /**
     * The most recent connection result.
     */
    val connectionTestResult: MutableState<ConnectionResult?> = mutableStateOf(null)

    private var completeOnSuccessfulTest = false

    init {
        uri = mutableStateOf(preferences.connectionUri)
        user = mutableStateOf(preferences.user)
        passwordInternal = mutableStateOf("")
        configuredPasswordExists = preferences.hasConfiguredPassword()
        passwordUnchanged = mutableStateOf(configuredPasswordExists)
    }

    /**
     * Sets the value of the [password] state.
     */
    fun setPassword(password: String) {
        this.passwordInternal.value = password
        this.passwordUnchanged.value = false
    }

    /**
     * Publishes a notification that this view is ready to exit.
     */
    fun finish() = onFinish()

    /**
     * Makes a connection test and if it's successful, applies the current configuration and exits.
     */
    fun apply() {
        completeOnSuccessfulTest = true
        testConnection()
    }

    /**
     * Applies the given configuration as defaults to [DatabaseConnection] and then exits.
     */
    private fun applyAndFinish(config: ConnectionConfiguration) {
        config.save(preferences, savePassword.value)
        DatabaseConnection.setDefaultConfiguration(config)
        finish()
    }

    /**
     * Makes a connection test for the currently entered values.
     */
    fun testConnection() {
        if (testingConnection.value) return
        testingConnection.value = true
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val config = createConfiguration()
                val result = config.testConnection()
                connectionTestResult.value = result
                if (completeOnSuccessfulTest && result == ConnectionResult.SUCCESS) {
                    applyAndFinish(config)
                }
            } catch (e: Exception) {
                when (e) {
                    is URISyntaxException,
                    is IllegalArgumentException -> {
                        connectionTestResult.value = ConnectionResult.INVALID_CONNECTION_STRING
                    }
                    else -> throw e
                }
            } finally {
                testingConnection.value = false
                completeOnSuccessfulTest = false
            }
        }
    }

    /**
     * Creates a [ConnectionConfiguration] object for the currently configured values.
     */
    private fun createConfiguration(): ConnectionConfiguration {
        if (passwordUnchanged.value) {
            return ConnectionConfiguration(
                uri.value,
                user.value,
                preferences
            )
        } else {
            return ConnectionConfiguration(
                uri.value,
                user.value,
                password.value.toCharArray()
            )
        }
    }

    private data class Configuration(
        val uri: String,
        val user: String,
        /**
         * The password or null if it is set to being kept unchanged.
         */
        val password: CharArray?,
        val savePassword: Boolean
    )

}