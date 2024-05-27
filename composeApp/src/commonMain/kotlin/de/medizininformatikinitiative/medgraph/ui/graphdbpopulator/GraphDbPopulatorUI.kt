package de.medizininformatikinitiative.medgraph.ui.graphdbpopulator

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import de.medizininformatikinitiative.medgraph.graphdbpopulator.GraphDbPopulator
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.theme.ApplicationTheme
import de.medizininformatikinitiative.medgraph.ui.theme.localColors
import de.medizininformatikinitiative.medgraph.ui.theme.templates.Button
import de.medizininformatikinitiative.medgraph.ui.theme.templates.TextField


@Composable
@Preview
fun GraphDbPopulatorUI() {
    ApplicationTheme {
        GraphDbPopulatorUI(
            GraphDbPopulatorScreenModel(), modifier = Modifier
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
        ) { Text(StringRes.cancel) }

        Button(
            viewModel::populate,
            enabled = !viewModel.executionUnderway
        ) { Text(StringRes.graph_db_populator_run) }
    }
}