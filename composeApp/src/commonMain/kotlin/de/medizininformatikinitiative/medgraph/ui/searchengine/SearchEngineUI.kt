package de.medizininformatikinitiative.medgraph.ui.searchengine

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.theme.ApplicationTheme
import de.medizininformatikinitiative.medgraph.ui.theme.templates.Button

@Composable
@Preview
private fun SearchEngineUI() {
    val viewModel = SearchEngineViewModel()
    viewModel.queryViewModel.queryText = "500 mg"
    viewModel.queryViewModel.productQueryText = "Aspirin"
    viewModel.queryViewModel.substanceQueryText = "ASS"
    viewModel.parseQuery()
    ApplicationTheme {
        SearchEngineUI(viewModel)
    }
}

@Composable
fun SearchEngineUI(viewModel: SearchEngineViewModel, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        RawAndParsedQueryUI(
            viewModel, modifier = Modifier
                .height(300.dp)
                .fillMaxWidth()
        )
        ParseAndExecuteButtonRow(viewModel, modifier = Modifier.fillMaxWidth())

        if (viewModel.queryExecutionUnderway) {
            Text("Executing Query...")
        }
        SearchResultsListUI(viewModel.queryResults, modifier = Modifier.weight(1f))
    }
}

@Composable
fun RawAndParsedQueryUI(viewModel: SearchEngineViewModel, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
    ) {
        QueryUI(viewModel.queryViewModel, modifier = Modifier.weight(1f))

        Divider(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxHeight()
                .width(1.dp)
        )

        val parsedQuery = viewModel.parsedQuery
        if (parsedQuery != null) {
            ParsedQueryUI(parsedQuery, modifier = Modifier.weight(1f))
        } else {
            Box(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun ParseAndExecuteButtonRow(viewModel: SearchEngineViewModel, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .padding(horizontal = 8.dp)
    ) {
        val navigator = LocalNavigator.currentOrThrow

        Button(onClick = { navigator.pop() }) {
            Text(StringRes.exit)
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = viewModel::parseQuery) {
                Text(StringRes.search_engine_dialog_parse)
            }
            Button(
                onClick = viewModel::executeQuery,
                enabled = viewModel.parsedQuery != null
            ) {
                Text(StringRes.search_engine_dialog_execute)
            }

            Button(
                onClick = { viewModel.parseQuery(); viewModel.executeQuery() },
            ) {
                Text(StringRes.search_engine_dialog_parse_execute)
            }
        }

    }
}
