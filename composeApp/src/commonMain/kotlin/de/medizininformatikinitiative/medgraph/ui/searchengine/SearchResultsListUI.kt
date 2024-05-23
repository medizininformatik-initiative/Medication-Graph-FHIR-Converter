package de.medizininformatikinitiative.medgraph.ui.searchengine

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.OriginalMatch
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Product
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Substance
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.searchengine.pipeline.MatchingObjectPipelineDisplay
import de.medizininformatikinitiative.medgraph.ui.theme.ApplicationTheme
import de.medizininformatikinitiative.medgraph.ui.theme.templates.Button

@Composable
@Preview
private fun SearchResultsListUI() {
    ApplicationTheme {
        SearchResultsListUI(
            listOf(
                OriginalMatch(Product(1, "Aspirin Bayer")),
                OriginalMatch(Product(2, "Aspirin HEXAL")),
                OriginalMatch(Substance(3, "Acetylsalicylsäure"))
            ), modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SearchResultsListUI(viewModel: SearchResultsListViewModel, modifier: Modifier = Modifier) {
    var currentPipelineInfo by remember { mutableStateOf<MatchingObject?>(null) }

    Column(modifier = modifier) {
        val actualLastSize = viewModel.actualLastQueryResultSize
        if (actualLastSize != null) {
            Text(
                StringRes.get(
                    StringRes.query_result_too_many_matches,
                    actualLastSize,
                    SearchResultsListViewModel.MAX_RESULT_SIZE
                ),
                color = MaterialTheme.colors.error,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        SearchResultsListUI(
            viewModel.queryResults,
            Modifier.padding(2.dp),
            onPipelineInfoRequested = { currentPipelineInfo = it })
    }

    val cpiState = currentPipelineInfo
    if (cpiState != null) {
        MatchingObjectPipelineInfoDialog(cpiState, { currentPipelineInfo = null })
    }
}

@Composable
fun MatchingObjectPipelineInfoDialog(target: MatchingObject, onDismissRequest: () -> Unit) {
    Dialog(onDismissRequest, DialogProperties(
        dismissOnBackPress = true,
        dismissOnClickOutside = true,
        usePlatformDefaultWidth = true
    )) {
        Column(
            modifier = Modifier
                .width(800.dp)
                .background(MaterialTheme.colors.background, shape = RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Text(StringRes.pipeline_info_title, textAlign = TextAlign.Center, style = MaterialTheme.typography.h4)
            Text(StringRes.pipeline_info_subtitle, style = MaterialTheme.typography.subtitle1)
            Divider(modifier = Modifier.fillMaxWidth(), thickness = 2.dp)
            Spacer(modifier = Modifier.height(4.dp))
            MatchingObjectPipelineDisplay(target, modifier = Modifier.fillMaxWidth())
        }

    }
}

@Composable
fun SearchResultsListUI(
    results: List<MatchingObject>,
    modifier: Modifier = Modifier,
    onPipelineInfoRequested: (MatchingObject) -> Unit = {},
) {
    var expandedResultIndex by remember { mutableStateOf<Int?>(null) }

    Row(
        modifier = modifier.fillMaxSize()
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .fillMaxHeight()
                .padding(4.dp)
        ) {
            results.forEachIndexed { index, result ->
                if (index != 0)
                    Divider(thickness = 1.dp, modifier = Modifier.padding(vertical = 2.dp))
                ExpandableMatchableObjectUI(
                    result.`object`,
                    expandedResultIndex == index,
                    onSwitchExpand = { expand ->
                        if (expand) expandedResultIndex = index else expandedResultIndex = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    bottomSlot = {
                        Row(horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()) {
                            Button(onClick = { onPipelineInfoRequested(result) }) {
                                Text(StringRes.search_result_show_pipeline_info)
                            }
                        }
                    }
                )
            }
        }
        VerticalScrollbar(
            modifier = Modifier
                .fillMaxHeight(),
            adapter = rememberScrollbarAdapter(scrollState)
        )
    }

}
