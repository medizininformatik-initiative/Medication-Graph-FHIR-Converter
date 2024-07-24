package de.medizininformatikinitiative.medgraph.ui.desktop.graphdbpopulator

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.*
import androidx.compose.runtime.Composable
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
import de.medizininformatikinitiative.medgraph.ui.theme.localColors
import de.medizininformatikinitiative.medgraph.ui.theme.templates.Button
import de.medizininformatikinitiative.medgraph.ui.theme.templates.ProgressIndication
import de.medizininformatikinitiative.medgraph.ui.tools.preview.TestOnlyProgressable
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
        viewModel.executionTask = TestOnlyProgressable()

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
        val annotatedNeo4jDesc = buildAnnotatedImportDirDescriptionText()
        ClickableText(
            annotatedNeo4jDesc,
            style = MaterialTheme.typography.body2,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            annotatedNeo4jDesc
                .getStringAnnotations("neo4jpath", it, it)
                .firstOrNull()?.let { stringAnnotation ->
                    viewModel.neo4jImportDirectory = StringRes.graph_db_populator_neo4j_import_dir_path
                }
        }
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

        Text(StringRes.graph_db_populator_wipe_warning, color = MaterialTheme.localColors.strongFailure)

        GraphDbPopulatorControls(viewModel, modifier.fillMaxWidth())

        Divider(thickness = 2.dp)

        ImportProgressUI(
            viewModel, modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .fillMaxWidth()
        )

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
    val task = viewModel.executionTask
    if (task != null) {
        ProgressIndication(task, modifier, viewModel.executionUnderway)
    }
}

@Composable
private fun buildAnnotatedImportDirDescriptionText() = buildAnnotatedString {
    append(StringRes.graph_db_populator_neo4j_import_dir_description_prefix)
    pushStringAnnotation(tag = "neo4jpath", annotation = StringRes.graph_db_populator_amice_stoffbez_description_link)
    withStyle(style = SpanStyle(color = MaterialTheme.colors.primary)) {
        append(StringRes.graph_db_populator_neo4j_import_dir_path)
    }
    pop()
    append(StringRes.graph_db_populator_neo4j_import_dir_description_suffix)
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
