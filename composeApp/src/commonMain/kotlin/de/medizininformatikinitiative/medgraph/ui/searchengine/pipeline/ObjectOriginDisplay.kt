package de.medizininformatikinitiative.medgraph.ui.searchengine.pipeline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import de.medizininformatikinitiative.medgraph.searchengine.matcher.EditDistanceSetMatcher
import de.medizininformatikinitiative.medgraph.searchengine.matcher.model.EditDistance
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchOrigin
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Origin
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.searchengine.query.TextBox
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
        is EditDistanceSetMatcher.Match -> SetMatcherOrigin(
            origin as MatchOrigin<EditDistanceSetMatcher.Match>,
            modifier
        )

        else -> UnknownOriginUI(modifier) // TODO Add generic matcher info
    }
}

@Composable
fun SetMatcherOrigin(origin: MatchOrigin<out EditDistanceSetMatcher.Match>, modifier: Modifier = Modifier) {
    // TODO Information about the matcher in general
    val match = origin.match
    val searchTermTokens = ArrayList(match.searchTerm.identifier)
    val targetTokens = ArrayList(match.matchedIdentifier.identifier.identifier)
    Column {
        for (distance in match.editDistances) {
            EditDistanceDisplay(distance, modifier = Modifier.fillMaxWidth())
            searchTermTokens.remove(distance.value1)
            targetTokens.remove(distance.value2)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column() {
                for (searchTermToken in searchTermTokens) TextBox(searchTermToken, modifier = Modifier.align(Alignment.Start))
            }
            Column() {
                for (targetToken in targetTokens) TextBox(targetToken, modifier = Modifier.align(Alignment.End))
            }
        }
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

@Composable
fun EditDistanceDisplay(editDistance: EditDistance, modifier: Modifier) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextBox(editDistance.value1)
        Text(StringRes.get(StringRes.pipeline_origin_edit_distance, editDistance.editDistance))
        TextBox(editDistance.value2)
    }
}