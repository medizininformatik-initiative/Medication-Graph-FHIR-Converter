package de.medizininformatikinitiative.medgraph.ui.desktop.graphdbpopulator

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.theme.ApplicationTheme
import de.medizininformatikinitiative.medgraph.ui.theme.templates.Button
import de.medizininformatikinitiative.medgraph.ui.theme.templates.TextField
import java.io.File
import javax.swing.JFileChooser


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
        DirectoryPathTextField(
            viewModel.mmiPharmindexDirectory,
            { value -> viewModel.mmiPharmindexDirectory = value },
            enabled = !viewModel.executionUnderway,
            label = StringRes.graph_db_populator_mmi_pharmindex_path,
            modifier = Modifier.fillMaxWidth()
        )
        DirectoryPathTextField(
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
            Text(
                errorMessage,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.error,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (viewModel.executionComplete) {
            Text(
                StringRes.graph_db_populator_done,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        ImportProgressUI(
            viewModel, modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .fillMaxWidth()
        )

    }
}

@Composable
private fun DirectoryPathTextField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    label: String,
    modifier: Modifier = Modifier,
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        TextField(
            value,
            onValueChange,
            modifier = Modifier.weight(1f),
            enabled = enabled,
            label = label
        )
        Button(
            onClick = {
                // This is a hack which completely blocks the UI thread. Not nice, but compose offers no simple file
                // chooser unfortunately
                val fc = JFileChooser()
                fc.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                val initialPath = if (value.isBlank()) System.getProperty("user.dir") else value
                fc.currentDirectory = File(initialPath)
                val returnVal = fc.showOpenDialog(null)
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    onValueChange(fc.selectedFile.absolutePath)
                }
            },
            enabled = enabled,
            modifier = Modifier.padding(4.dp)
        ) {
            Text(StringRes.browse)
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
        ) { Text(StringRes.do_return) }

        Button(
            viewModel::populate,
            enabled = !viewModel.executionUnderway && !viewModel.executionComplete
        ) { Text(StringRes.graph_db_populator_run) }
    }
}

@Composable
private fun ImportProgressUI(viewModel: GraphDbPopulatorScreenModel, modifier: Modifier = Modifier) {
    if (viewModel.executionUnderway) {
        Column(
            modifier = modifier
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(viewModel.executionMajorStep)
                Spacer(modifier = Modifier.width(8.dp))
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
            }
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
