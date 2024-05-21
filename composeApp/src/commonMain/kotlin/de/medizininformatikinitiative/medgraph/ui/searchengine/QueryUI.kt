package de.medizininformatikinitiative.medgraph.ui.searchengine

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.theme.ApplicationTheme
import de.medizininformatikinitiative.medgraph.ui.theme.templates.Button
import de.medizininformatikinitiative.medgraph.ui.theme.templates.TextField


@Composable
@Preview
fun QueryUI() {
    ApplicationTheme {
        QueryUI(QueryViewModel(), modifier = Modifier.padding(8.dp))
    }
}

@Composable
fun QueryUI(
    viewModel: QueryViewModel,
    onParseQuery: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {

        TextField(viewModel.queryText, label = StringRes.query_dialog_query_text, modifier = Modifier.fillMaxWidth())
        TextField(
            viewModel.productQueryText,
            label = StringRes.query_dialog_product_query_text,
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            viewModel.substanceQueryText,
            label = StringRes.query_dialog_substance_query_text,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Button(onClick = onParseQuery) {
                Text(StringRes.query_dialog_parse)
            }
        }

    }
}