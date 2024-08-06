package de.medizininformatikinitiative.medgraph.ui.desktop.fhirexporter

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import de.medizininformatikinitiative.medgraph.ui.desktop.templates.PathTextField
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.theme.ApplicationTheme
import de.medizininformatikinitiative.medgraph.ui.theme.localColors
import de.medizininformatikinitiative.medgraph.ui.theme.templates.Button
import de.medizininformatikinitiative.medgraph.ui.theme.templates.DetailedProgressIndication
import de.medizininformatikinitiative.medgraph.ui.tools.preview.TestOnlyProgressable
import javax.swing.JFileChooser


@Composable
@Preview
private fun FhirExporterUI() {
    ApplicationTheme {
        val viewModel = FhirExporterScreenModel()
        viewModel.exportUnderway = true
        viewModel.exportTask.bind(TestOnlyProgressable())
        FhirExporterUI(viewModel, Modifier.fillMaxWidth().padding(8.dp))
    }
}

@Composable
fun FhirExporterUI(viewModel: FhirExporterScreenModel, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(StringRes.fhir_exporter_description)
        PathTextField(
            viewModel.exportPath,
            { v -> viewModel.exportPath = v },
            enabled = !viewModel.exportUnderway,
            label = StringRes.fhir_exporter_export_path,
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val navigator = LocalNavigator.current
            Button(
                onClick = { navigator!!.pop() },
                enabled = !viewModel.exportUnderway
            ) {
                Text(StringRes.do_return)
            }
            Button(
                onClick = { viewModel.doExport() },
                enabled = !viewModel.exportUnderway
            ) {
                Text(StringRes.fhir_exporter_do_export)
            }
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp), thickness = 2.dp)

        if (viewModel.exportUnderway) {
            val exportTask = viewModel.exportTask
            if (exportTask.progressable != null)
                DetailedProgressIndication(exportTask)
        } else {
            val error = viewModel.errorText
            if (error != null)
                Text(error, color = MaterialTheme.localColors.error)
        }
    }
}