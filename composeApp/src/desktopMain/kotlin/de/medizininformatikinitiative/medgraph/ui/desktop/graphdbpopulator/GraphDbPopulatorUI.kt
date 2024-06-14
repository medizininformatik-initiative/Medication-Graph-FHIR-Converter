package de.medizininformatikinitiative.medgraph.ui.desktop.graphdbpopulator

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import de.medizininformatikinitiative.medgraph.ui.desktop.templates.PathTextField
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.theme.ApplicationTheme
import de.medizininformatikinitiative.medgraph.ui.theme.templates.Button
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter


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
        PathTextField(
            viewModel.mmiPharmindexDirectory,
            { value -> viewModel.mmiPharmindexDirectory = value },
            enabled = !viewModel.executionUnderway,
            label = StringRes.graph_db_populator_mmi_pharmindex_path,
            modifier = Modifier.fillMaxWidth(),
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        )
        PathTextField(
            viewModel.neo4jImportDirectory,
            { value -> viewModel.neo4jImportDirectory = value },
            enabled = !viewModel.executionUnderway,
            label = StringRes.graph_db_populator_neo4j_import_dir,
            modifier = Modifier.fillMaxWidth(),
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        )
        Text(
            StringRes.graph_db_populator_neo4j_import_dir_description,
            style = MaterialTheme.typography.body2,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        PathTextField(
            viewModel.amiceStoffBezFile,
            { value -> viewModel.amiceStoffBezFile = value },
            enabled = !viewModel.executionUnderway,
            label = StringRes.graph_db_populator_amice_stoffbez_path,
            modifier = Modifier.fillMaxWidth(),
            fileSelectionMode = JFileChooser.FILES_ONLY,
            fileFilter = FileNameExtensionFilter("CSV files", "csv")
        )

        val uriHandler = LocalUriHandler.current
        val annotatedDescString = buildAnnotatedAmiceStoffBezDescriptionText()
        ClickableText(
            annotatedDescString,
            modifier = Modifier.padding(horizontal = 8.dp),
            style = MaterialTheme.typography.body2,
        ) {
            annotatedDescString
                .getStringAnnotations("link", it, it)
                .firstOrNull()?.let { stringAnnotation ->
                    uriHandler.openUri(stringAnnotation.item)
                }
        }

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

@Composable
private fun buildAnnotatedAmiceStoffBezDescriptionText() = buildAnnotatedString {
    append(StringRes.graph_db_populator_amice_stoffbez_description_p1)
    pushStringAnnotation(tag = "link", annotation = StringRes.graph_db_populator_amice_stoffbez_description_link)
    withStyle(style = SpanStyle(color = MaterialTheme.colors.primary)) {
        append(StringRes.graph_db_populator_amice_stoffbez_description_link_text)
    }
    pop()
    append(StringRes.graph_db_populator_amice_stoffbez_description_p2)
}
