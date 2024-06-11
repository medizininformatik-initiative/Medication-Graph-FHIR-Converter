package de.medizininformatikinitiative.medgraph.ui.searchengine.pipeline

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import de.medizininformatikinitiative.medgraph.searchengine.matcher.EditDistanceSetMatcher
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchOrigin
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Origin
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.searchengine.pipeline.matcher.EditDistanceSetMatcherUI
import de.medizininformatikinitiative.medgraph.ui.theme.templates.clipToBox


@Composable
fun OriginUI(origin: Origin, modifier: Modifier = Modifier) {
    when (origin) {
        is MatchOrigin<*> -> MatchOriginUI(origin, modifier)
        else -> UnknownOriginUI(modifier)
    }
}

@Suppress("UNCHECKED_CAST")
@Composable
fun MatchOriginUI(origin: MatchOrigin<*>, modifier: Modifier = Modifier) {
    when (origin.match) {
        is EditDistanceSetMatcher.Match -> EditDistanceSetMatcherUI(
            origin as MatchOrigin<EditDistanceSetMatcher.Match>,
            modifier
        )

        else -> UnknownOriginUI(modifier) // TODO Add generic matcher info
    }
}

@Composable
fun UnknownOriginUI(modifier: Modifier = Modifier) {
    Card(modifier) {
        Text(StringRes.unknown_origin, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
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
