package de.medizininformatikinitiative.medgraph.ui.searchengine

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.OriginalMatch
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Product
import de.medizininformatikinitiative.medgraph.ui.theme.ApplicationTheme
import javax.lang.model.util.Elements.Origin


@Composable
@Preview
private fun SearchResultUI() {
    ApplicationTheme {
        SearchResultUI(OriginalMatch(Product(1, "Furorese 100mg")), modifier = Modifier.fillMaxWidth())
    }
}

/**
 * Displays a single search result.
 */
@Composable
fun SearchResultUI(result: MatchingObject, modifier: Modifier = Modifier) {
    TextBox(result.`object`.name, modifier = modifier)
}