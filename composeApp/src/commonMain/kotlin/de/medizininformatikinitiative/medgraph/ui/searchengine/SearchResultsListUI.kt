package de.medizininformatikinitiative.medgraph.ui.searchengine

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.OriginalMatch
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Product
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Substance
import de.medizininformatikinitiative.medgraph.ui.theme.ApplicationTheme

@Composable
private fun SearchResultsListUI() {
    ApplicationTheme {
        SearchResultsListUI(listOf(
            OriginalMatch(Product(1, "Aspirin Bayer")),
            OriginalMatch(Product(2, "Aspirin Bayer")),
            OriginalMatch(Substance(3, "Acetylsalicylsäure"))
        ), modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun SearchResultsListUI(results: List<MatchingObject>, modifier: Modifier = Modifier) {

}
