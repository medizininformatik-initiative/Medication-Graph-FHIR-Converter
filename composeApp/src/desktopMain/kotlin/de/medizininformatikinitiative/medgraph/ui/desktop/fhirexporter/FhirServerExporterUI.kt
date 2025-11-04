package de.medizininformatikinitiative.medgraph.ui.desktop.fhirexporter

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.theme.ApplicationTheme
import de.medizininformatikinitiative.medgraph.ui.theme.templates.Button
import de.medizininformatikinitiative.medgraph.ui.theme.templates.TextField
import de.medizininformatikinitiative.medgraph.ui.tools.preview.TestOnlyProgressable


@Composable
@Preview
private fun FhirServerExporterUI() {
    ApplicationTheme {
        val viewModel = FhirServerFhirExporterScreenModel()
        viewModel.exportUnderway = true
        viewModel.exportTask.bind(TestOnlyProgressable())
        FhirServerExporterUI(viewModel, Modifier.fillMaxWidth().padding(8.dp))
    }
}

@Composable
fun FhirServerExporterUI(viewModel: FhirServerFhirExporterScreenModel, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(StringRes.fhir_server_exporter_description)

        Spacer(modifier = Modifier.height(4.dp))
        Text(StringRes.fhir_server_exporter_fhir_url, fontWeight = FontWeight.Bold)
        TextField(
            viewModel.fhirBaseUrl,
            label = StringRes.fhir_exporter_fhir_server_url,
            modifier = Modifier.fillMaxWidth()
        )

        Divider(modifier = Modifier.padding(vertical = 4.dp), thickness = 2.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(StringRes.fhir_server_exporter_description_no_auth)

            Button(
                onClick = { viewModel.exportNoAuth() },
                enabled = !viewModel.exportUnderway
            ) {
                Text(StringRes.fhir_exporter_export_no_auth)
            }
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp), thickness = 1.dp)

        Text(StringRes.fhir_server_exporter_description_basic_auth)

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            TextField(
                viewModel.username,
                label = StringRes.fhir_exporter_http_basic_auth_username,
                modifier = Modifier.weight(1f)
            )
            TextField(
                viewModel.password,
                label = StringRes.fhir_exporter_http_basic_auth_password,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = { viewModel.exportBasicAuth() },
                enabled = !viewModel.exportUnderway
            ) {
                Text(StringRes.fhir_exporter_export_http_basic_auth)
            }
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp), thickness = 1.dp)

        Text(StringRes.fhir_server_exporter_description_token_auth)
        TextField(
            viewModel.bearerToken,
            label = StringRes.fhir_exporter_bearer_token,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = { viewModel.exportTokenAuth() },
                enabled = !viewModel.exportUnderway
            ) {
                Text(StringRes.fhir_exporter_export_token_auth)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Divider(modifier = Modifier.padding(vertical = 4.dp), thickness = 2.dp)

        val navigator = LocalNavigator.current
        Button(
            onClick = { navigator!!.pop() },
            enabled = !viewModel.exportUnderway
        ) {
            Text(StringRes.do_return)
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp), thickness = 2.dp)

        ExportProgressIndication(viewModel)
    }
}