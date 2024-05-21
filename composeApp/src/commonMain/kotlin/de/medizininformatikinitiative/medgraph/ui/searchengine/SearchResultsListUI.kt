package de.medizininformatikinitiative.medgraph.ui.searchengine

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxHeight()
    ) {
        results.forEachIndexed { index, result ->
            if (index != 0)
                Divider(thickness = 1.dp)
            SearchResultUI(result, modifier = Modifier.fillMaxWidth())
        }
    }

}
