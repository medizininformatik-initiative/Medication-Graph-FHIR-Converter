package de.medizininformatikinitiative.medgraph.ui.searchengine.pipeline.origin

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.initial.ApacheLuceneInitialMatchFinder.ApacheLuceneOrigin
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.DosageDetectorOrigin
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchOrigin
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Origin
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.theme.templates.clipToBox


@Composable
fun OriginUI(origin: Origin, modifier: Modifier = Modifier) {
    when (origin) {
        is MatchOrigin<*> -> MatchOriginUI(origin, modifier)
        is DosageDetectorOrigin -> DosageDetectorOriginUI(origin, modifier)
        is ApacheLuceneOrigin -> TextOnlyOriginUI(StringRes.apache_lucene_origin, modifier)
        else -> TextOnlyOriginUI(StringRes.unknown_origin, modifier)
    }
}

@Composable
fun TextOnlyOriginUI(text: String, modifier: Modifier = Modifier) {
    Card(modifier) {
        Text(text, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun Card(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.surface,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier.clipToBox(backgroundColor)
    ) {
        content()
    }
}
