@file:OptIn(ExperimentalResourceApi::class)

package de.medizininformatikinitiative.medgraph.ui.common.db

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import de.medizininformatikinitiative.medgraph.common.ApplicationPreferences
import de.medizininformatikinitiative.medgraph.common.db.ConnectionConfiguration
import kotlinx.coroutines.GlobalScope
import medicationgraphfhirconverter.composeapp.generated.resources.*
import medicationgraphfhirconverter.composeapp.generated.resources.Res
import medicationgraphfhirconverter.composeapp.generated.resources.db_connection_dialog_password
import medicationgraphfhirconverter.composeapp.generated.resources.db_connection_dialog_uri
import medicationgraphfhirconverter.composeapp.generated.resources.db_connection_dialog_user
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource


@Composable
@Preview
private fun ConnectionDialogUI() {
    val viewModel = ConnectionDialogViewModel(
        ApplicationPreferences.getDatabaseConnectionPreferences(),
        GlobalScope
    )
    ConnectionDialog(viewModel, Modifier.padding(8.dp))
}

/**
 * Displays the configuration dialog for the database connection options
 *
 * @author Markus Budeus
 */
@Composable
fun ConnectionDialog(viewModel: ConnectionDialogViewModel, modifier: Modifier = Modifier) {
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
    OutlinedTextField(
        uri, { value -> uri = value },
        label = { Text(stringResource(Res.string.db_connection_dialog_uri)) },
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        user, { value -> user = value },
        label = { Text(stringResource(Res.string.db_connection_dialog_user)) },
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        password, { value -> viewModel.setPassword(value) },
        label = {
            Text(
                stringResource(Res.string.db_connection_dialog_password) +
                        (if (passwordUnchanged && !passwordFocus)
                            " " + stringResource(Res.string.db_connection_dialog_password_unchanged) else "")
            )
        },
        placeholder = {
            if (passwordUnchanged) Text(stringResource(Res.string.db_connection_dialog_password_unchanged))
        },
        visualTransformation = if (passwordUnchanged) VisualTransformation.None else PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth()
            .onFocusChanged { focusState -> passwordFocus = focusState.hasFocus }
    )
}

@Composable
private fun ButtonRow(viewModel: ConnectionDialogViewModel, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
    ) {
        Button(
            { viewModel.finish() },
        ) {
            Text(stringResource(Res.string.cancel))
        }
        Button(
            { viewModel.testConnection() },
            enabled = !viewModel.testingConnection.value
        ) {
            Text(stringResource(Res.string.db_connection_dialog_test_connection))
        }
        Button(
            { viewModel.apply() },
            enabled = !viewModel.testingConnection.value
        ) {
            Text(stringResource(Res.string.ok))
        }
    }
}

@Composable
private fun ConnectionTestStatus(viewModel: ConnectionDialogViewModel, modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        if (viewModel.testingConnection.value) {
            Text(stringResource(Res.string.db_connection_dialog_test_underway))
        } else {
            when (viewModel.connectionTestResult.value) {
                ConnectionConfiguration.ConnectionResult.SUCCESS -> {
                    Text(stringResource(Res.string.db_connection_dialog_test_success))
                }

                ConnectionConfiguration.ConnectionResult.INVALID_CONNECTION_STRING -> {
                    Text(stringResource(Res.string.db_connection_dialog_test_invalid_connection_string))
                }

                ConnectionConfiguration.ConnectionResult.SERVICE_UNAVAILABLE -> {
                    Text(stringResource(Res.string.db_connection_dialog_test_service_unavailable))
                }

                ConnectionConfiguration.ConnectionResult.AUTHENTICATION_FAILED -> {
                    Text(stringResource(Res.string.db_connection_dialog_test_authentication_failed))
                }

                null -> {
                }
            }
        }
    }
}