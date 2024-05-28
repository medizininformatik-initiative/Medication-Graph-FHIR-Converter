package de.medizininformatikinitiative.medgraph.ui.graphdbpopulator

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.AwtWindow
import cafe.adriel.voyager.navigator.LocalNavigator
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.theme.ApplicationTheme
import de.medizininformatikinitiative.medgraph.ui.theme.templates.Button
import de.medizininformatikinitiative.medgraph.ui.theme.templates.TextField
import java.awt.FileDialog
import java.awt.Frame


@Composable
@Preview
fun GraphDbPopulatorUI() {
    ApplicationTheme {
        val viewModel = GraphDbPopulatorScreenModel()
        viewModel.neo4jImportDirectory = "/var/lib/neo4j/import"
        viewModel.errorMessage = "Something went wrong, but this is only a test."

        viewModel.executionUnderway = true
        viewModel.executionMajorStep = "Running ProductsLoader"
        viewModel.executionMinorStep = "Importing products from CSV"
        viewModel.executionMajorStepIndex = 3
        viewModel.executionTotalMajorStepsNumber = 8

        GraphDbPopulatorUI(
            viewModel, modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
    }
}

@Composable
fun GraphDbPopulatorUI(viewModel: GraphDbPopulatorScreenModel, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        TextField(
            viewModel.mmiPharmindexDirectory,
            { value -> viewModel.mmiPharmindexDirectory = value },
            enabled = !viewModel.executionUnderway,
            label = StringRes.graph_db_populator_mmi_pharmindex_path,
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            viewModel.neo4jImportDirectory,
            { value -> viewModel.neo4jImportDirectory = value },
            enabled = !viewModel.executionUnderway,
            label = StringRes.graph_db_populator_neo4j_import_dir,
            modifier = Modifier.fillMaxWidth()
        )
        Text(StringRes.graph_db_populator_neo4j_import_dir_description, modifier = Modifier.padding(horizontal = 8.dp))

        GraphDbPopulatorControls(viewModel, modifier.fillMaxWidth())

        Divider(thickness = 2.dp)

        val errorMessage = viewModel.errorMessage
        if (errorMessage != null) {
            Text(errorMessage, color = MaterialTheme.colors.error)
        }

        ImportProgressUI(viewModel, modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxWidth())

    }
}

@Composable
private fun GraphDbPopulatorControls(viewModel: GraphDbPopulatorScreenModel, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
    ) {
        val localNavigator = LocalNavigator.current
        Button(
            { localNavigator!!.pop() },
            enabled = !viewModel.executionUnderway
        ) { Text(StringRes.do_return) }

        Button(
            viewModel::populate,
            enabled = !viewModel.executionUnderway
        ) { Text(StringRes.graph_db_populator_run) }
    }
}

@Composable
private fun ImportProgressUI(viewModel: GraphDbPopulatorScreenModel, modifier: Modifier = Modifier) {
    if (viewModel.executionUnderway) {
        Column(
            modifier = modifier
        ) {
            Text(viewModel.executionMajorStep)
            LinearProgressIndicator(
                progress =
                viewModel.executionMajorStepIndex * 1f / viewModel.executionTotalMajorStepsNumber,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .height(12.dp)
            )
            val minorStep = viewModel.executionMinorStep
            if (minorStep != null)
                Text(minorStep, modifier = Modifier.padding(horizontal = 8.dp))
        }
    }
}
