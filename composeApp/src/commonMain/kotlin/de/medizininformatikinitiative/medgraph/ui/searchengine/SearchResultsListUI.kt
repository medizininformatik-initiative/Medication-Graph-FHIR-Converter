package de.medizininformatikinitiative.medgraph.ui.searchengine

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.DetailedProduct
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.OriginalMatch
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Product
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Substance
import de.medizininformatikinitiative.medgraph.ui.theme.ApplicationTheme

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
fun SearchResultsListUI(results: List<MatchingObject>, modifier: Modifier = Modifier) {
    var expandedResultIndex by remember { mutableStateOf<Int?>(null) }

    Row(
        modifier = modifier.fillMaxSize()
            .padding(4.dp)
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .fillMaxHeight()
        ) {
            results.forEachIndexed { index, result ->
                if (index != 0)
                    Divider(thickness = 1.dp)
                val obj = result.`object`
                if (obj is DetailedProduct) {
                    ExpandableDetailedProductUI(obj,
                        expandedResultIndex == index,
                        onSwitchExpand = { expand -> if (expand) expandedResultIndex = index else expandedResultIndex = null },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    MatchableObjectUI(result.`object`, modifier = Modifier.fillMaxWidth())
                }
            }
        }
        VerticalScrollbar(
            modifier = Modifier
                .fillMaxHeight(),
            adapter = rememberScrollbarAdapter(scrollState)
        )
    }

}
