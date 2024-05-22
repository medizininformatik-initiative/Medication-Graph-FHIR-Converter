package de.medizininformatikinitiative.medgraph.ui.searchengine

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.isTypedEvent
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.theme.ApplicationTheme
import de.medizininformatikinitiative.medgraph.ui.theme.templates.Button
import de.medizininformatikinitiative.medgraph.ui.theme.templates.TextField
import de.medizininformatikinitiative.medgraph.ui.tools.captureEnterPress


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
    modifier: Modifier = Modifier,
    onEnterPressed: () -> Unit = {}
) {
    Column(
        modifier = modifier
    ) {

        TextField(
            viewModel.queryText,
            { v -> viewModel.queryText = v },
            label = StringRes.query_dialog_query_text,
            modifier = Modifier.fillMaxWidth()
                .captureEnterPress(onEnterPressed)
        )
        TextField(
            viewModel.productQueryText,
            { v -> viewModel.productQueryText = v },
            label = StringRes.query_dialog_product_query_text,
            modifier = Modifier.fillMaxWidth()
                .captureEnterPress(onEnterPressed)
        )
        TextField(
            viewModel.substanceQueryText,
            { v -> viewModel.substanceQueryText = v },
            label = StringRes.query_dialog_substance_query_text,
            modifier = Modifier.fillMaxWidth()
                .captureEnterPress(onEnterPressed)
        )
    }
}