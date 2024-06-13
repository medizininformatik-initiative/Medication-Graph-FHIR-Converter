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
import de.medizininformatikinitiative.medgraph.ui.searchengine.query.QueryUI
import de.medizininformatikinitiative.medgraph.ui.searchengine.query.RefinedQueryUI
import de.medizininformatikinitiative.medgraph.ui.searchengine.results.SearchResultsListUI
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
    viewModel.refineQuery()
    ApplicationTheme {
        SearchEngineUI(viewModel)
    }
}

@Composable
fun SearchEngineUI(viewModel: SearchEngineViewModel, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        RawAndParsedQueryUI(
            viewModel, modifier = Modifier
                .height(280.dp)
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
            modifier = Modifier.weight(1.5f),
            onEnterPressed = viewModel::refineAndExecuteQuery,
        )

        Divider(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .fillMaxHeight()
                .width(1.dp)
        )

        val parsedQuery = viewModel.refinedQuery
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            if (viewModel.queryRefiningUnderway) {
                CircularProgressIndicator(
                    modifier = Modifier.width(48.dp)
                        .align(Alignment.Center),
                    color = CorporateDesign.Main.TUMBlue,
                    backgroundColor = CorporateDesign.Secondary.LighterBlue
                )
            } else if (parsedQuery != null) {
                RefinedQueryUI(parsedQuery, modifier = Modifier.fillMaxWidth())
            }
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
            Button(
                onClick = viewModel::refineQuery,
                enabled = !viewModel.busy
            ) {
                Text(StringRes.search_engine_dialog_parse)
            }
            Button(
                onClick = viewModel::executeQuery,
                enabled = viewModel.refinedQuery != null && !viewModel.busy
            ) {
                Text(StringRes.search_engine_dialog_execute)
            }

            Button(
                onClick = { viewModel.refineAndExecuteQuery() },
                enabled = !viewModel.busy,
            ) {
                Text(StringRes.search_engine_dialog_parse_execute)
            }
        }

    }
}
