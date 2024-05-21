package de.medizininformatikinitiative.medgraph.ui.searchengine

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.medizininformatikinitiative.medgraph.searchengine.model.Dosage
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.theme.ApplicationTheme
import de.medizininformatikinitiative.medgraph.ui.theme.templates.Button
import java.math.BigDecimal

@Composable
@Preview
private fun SearchEngineUI() {
    val viewModel = SearchEngineViewModel()
    viewModel.parsedQuery = SearchQuery("Aspirin", "ASS", listOf(Dosage.of(500, "mg")), emptyList())
    ApplicationTheme {
        SearchEngineUI(viewModel)
    }
}

@Composable
fun SearchEngineUI(viewModel: SearchEngineViewModel, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        RawAndParsedQueryUI(viewModel, modifier = Modifier
            .height(300.dp)
            .fillMaxWidth())
        ParseAndExecuteButtonRow(viewModel, modifier = Modifier.fillMaxWidth())
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
        horizontalArrangement = Arrangement.End,
        modifier = modifier
            .padding(horizontal = 8.dp)
    ) {
        Button(onClick = viewModel::parseQuery) {
            Text(StringRes.query_dialog_parse)
        }
    }
}
