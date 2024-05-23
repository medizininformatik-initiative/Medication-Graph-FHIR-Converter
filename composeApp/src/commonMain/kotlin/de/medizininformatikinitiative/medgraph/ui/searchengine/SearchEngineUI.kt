package de.medizininformatikinitiative.medgraph.ui.searchengine

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.theme.ApplicationTheme
import de.medizininformatikinitiative.medgraph.ui.theme.CorporateDesign
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
                .height(250.dp)
                .fillMaxWidth()
        )
        ParseAndExecuteButtonRow(viewModel, modifier = Modifier.fillMaxWidth())

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(2.dp, MaterialTheme.colors.onBackground, RoundedCornerShape(4.dp))
        ) {
            if (viewModel.queryExecutionUnderway) {
                CircularProgressIndicator(
                    modifier = Modifier.width(64.dp)
                        .align(Alignment.Center),
                    color = CorporateDesign.Main.TUMBlue,
                    backgroundColor = CorporateDesign.Secondary.LighterBlue
                )
            } else {
                SearchResultsListUI(viewModel.resultsViewModel)
            }
        }
    }
}

@Composable
fun RawAndParsedQueryUI(viewModel: SearchEngineViewModel, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
    ) {
        QueryUI(
            viewModel.queryViewModel,
            modifier = Modifier.weight(1f),
            onEnterPressed = viewModel::parseAndExecuteQuery,
        )

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
                enabled = viewModel.parsedQuery != null && !viewModel.queryExecutionUnderway
            ) {
                Text(StringRes.search_engine_dialog_execute)
            }

            Button(
                onClick = { viewModel.parseAndExecuteQuery() },
                enabled = !viewModel.queryExecutionUnderway,
            ) {
                Text(StringRes.search_engine_dialog_parse_execute)
            }
        }

    }
}
