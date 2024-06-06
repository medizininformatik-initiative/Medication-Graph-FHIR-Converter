package de.medizininformatikinitiative.medgraph.ui.common.db

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import de.medizininformatikinitiative.medgraph.common.db.ConnectionConfiguration
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes

@Composable
@Preview
private fun ConnectionDialogUI() {
    ConnectionDialogUI(ConnectionDialogViewModel(), Modifier.padding(8.dp))
}

/**
 * Displays the configuration dialog for the database connection options
 *
 * @author Markus Budeus
 */
@Composable
fun ConnectionDialogUI(viewModel: ConnectionDialogViewModel, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
    ) {
        ConnectionInfoTextFields(viewModel)

        ButtonRow(viewModel, Modifier.fillMaxWidth())

        ConnectionTestStatus(viewModel, Modifier.padding(start = 4.dp))
    }
}

@Composable
private fun ConnectionInfoTextFields(viewModel: ConnectionDialogViewModel) {
    var uri by viewModel.uri
    var user by viewModel.user
    val password by viewModel.password
    val passwordUnchanged by viewModel.passwordUnchanged
    var passwordFocus by remember { mutableStateOf(false) }
    val testingConnection by viewModel.testingConnection

    OutlinedTextField(
        uri, { value -> uri = value },
        enabled = !testingConnection,
        label = { Text(StringRes.db_connection_dialog_uri) },
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        user, { value -> user = value },
        enabled = !testingConnection,
        label = { Text(StringRes.db_connection_dialog_user) },
        modifier = Modifier.fillMaxWidth()
    )


    Row(verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {

        OutlinedTextField(
            password, { value -> viewModel.setPassword(value) },
            enabled = !testingConnection,
            label = {
                Text(
                    StringRes.db_connection_dialog_password +
                            (if (passwordUnchanged && !passwordFocus)
                                " " + StringRes.db_connection_dialog_password_unchanged else "")
                )
            },
            placeholder = {
                if (passwordUnchanged) Text(StringRes.db_connection_dialog_password_unchanged)
            },
            visualTransformation = if (passwordUnchanged) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.weight(1f)
                .onFocusChanged { focusState -> passwordFocus = focusState.hasFocus }
        )

        Checkbox(viewModel.savePassword.value, { checked -> viewModel.savePassword.value = checked })
        Text(StringRes.db_connection_dialog_save_password)
    }

}

@Composable
private fun ButtonRow(viewModel: ConnectionDialogViewModel, modifier: Modifier = Modifier) {
    val navigator = LocalNavigator.currentOrThrow
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
    ) {
        Button(
            { navigator.popAll() },
        ) {
            Text(StringRes.cancel)
        }
        Button(
            { viewModel.testConnection() },
            enabled = !viewModel.testingConnection.value
        ) {
            Text(StringRes.db_connection_dialog_test_connection)
        }
        Button(
            { viewModel.apply().thenAccept { success -> if (success) navigator.pop() } },
            enabled = !viewModel.testingConnection.value
        ) {
            Text(StringRes.ok)
        }
    }
}

@Composable
private fun ConnectionTestStatus(viewModel: ConnectionDialogViewModel, modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        if (viewModel.testingConnection.value) {
            Text(StringRes.db_connection_dialog_test_underway)
        } else {
            when (viewModel.connectionTestResult.value) {
                ConnectionConfiguration.ConnectionResult.SUCCESS -> {
                    Text(StringRes.db_connection_dialog_test_success)
                }

                ConnectionConfiguration.ConnectionResult.INVALID_CONNECTION_STRING -> {
                    Text(StringRes.db_connection_dialog_test_invalid_connection_string)
                }

                ConnectionConfiguration.ConnectionResult.SERVICE_UNAVAILABLE -> {
                    Text(StringRes.db_connection_dialog_test_service_unavailable)
                }

                ConnectionConfiguration.ConnectionResult.AUTHENTICATION_FAILED -> {
                    Text(StringRes.db_connection_dialog_test_authentication_failed)
                }

                null -> {
                }
            }
        }
    }
}