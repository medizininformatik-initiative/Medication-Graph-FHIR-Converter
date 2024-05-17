package de.medizininformatikinitiative.medgraph.ui.common.db

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import de.medizininformatikinitiative.medgraph.common.ApplicationPreferences
import de.medizininformatikinitiative.medgraph.common.db.ConnectionConfiguration
import de.medizininformatikinitiative.medgraph.common.db.ConnectionConfiguration.ConnectionResult
import de.medizininformatikinitiative.medgraph.common.db.ConnectionPreferences
import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnection
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.CompletableFuture

/**
 * Manages the state of the database connection dialog.
 *
 * @author Markus Budeus
 */
class ConnectionDialogViewModel(
    private val preferences: ConnectionPreferences = ApplicationPreferences.getDatabaseConnectionPreferences(),
) : ScreenModel {

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
     * Makes a connection test and if it's successful, applies the current configuration and exits.
     */
    fun apply(): CompletableFuture<Boolean> = wrapIntoFuture { applyInternal() }

    /**
     * Makes a connection test and writes its results into the screen model state.
     *
     * @return a future which completes once the test is complete
     */
    fun testConnection(): CompletableFuture<Boolean> = wrapIntoFuture {
        if (testingConnection.value) return@wrapIntoFuture false
        testConnection(createConfiguration())
    }

    /**
     * Makes a connection test and if it's successful, applies the current configuration and exits.
     */
    private suspend fun applyInternal(): Boolean {
        val config = createConfiguration();
        if (testConnection(config)) {
            config.save(preferences, savePassword.value)
            DatabaseConnection.setDefaultConfiguration(config)
            return true
        }
        return false
    }

    /**
     * Makes a connection test for the currently entered values.
     *
     * @return true if the connection test was successful and false if it wasn't
     */
    suspend fun testConnection(config: ConnectionConfiguration): Boolean {
        testingConnection.value = true
        try {
            delay(1000)
            val result = config.testConnection()
            connectionTestResult.value = result
            return result == ConnectionResult.SUCCESS;
        } finally {
            testingConnection.value = false
            completeOnSuccessfulTest = false
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

    /**
     * Asynchronously executes the given suspending function using this screen model's coroutine scope. The result
     * of the action will then be published using the returned CompletableFuture.
     *
     * @param action the action to asynchronously execute
     * @param targetDispatcher the target dispatcher on which to execute the suspending function
     * @param callbackOnMainThread if true, the result is published to the future using the main thread, thereby
     * ensuring any follow-up calls on that future happen on the main thread
     */
    protected fun <T> wrapIntoFuture(
        targetDispatcher: CoroutineDispatcher = Dispatchers.IO,
        callbackOnMainThread: Boolean = true,
        action: suspend () -> T,
    ): CompletableFuture<T> {
        val future = CompletableFuture<T>()
        screenModelScope.launch(targetDispatcher) {
            var completionAction: () -> Unit
            try {
                val result = action.invoke()
                completionAction = { future.complete(result) }
            } catch (e: Exception) {
                completionAction = { future.completeExceptionally(e) }
            }

            if (callbackOnMainThread) {
                launch(Dispatchers.Main) { completionAction() }
            } else {
                completionAction()
            }

        }
        return future
    }
}