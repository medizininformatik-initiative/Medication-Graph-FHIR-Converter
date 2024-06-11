package de.medizininformatikinitiative.medgraph.ui.searchengine.pipeline

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.medizininformatikinitiative.medgraph.searchengine.matcher.EditDistanceSetMatcher
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Identifiable
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.Identifier
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchOrigin
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Origin
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.searchengine.pipeline.identifier.IdentifierUI
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

    TwoIdentifierSources(
        origin.match.searchTerm,
        origin.match.matchedIdentifier.identifier,
        target2 = origin.match.matchedIdentifier.target
    )
    when (origin.match) {
        is EditDistanceSetMatcher.Match -> EditDistanceSetMatcherUI(
            origin as MatchOrigin<EditDistanceSetMatcher.Match>,
            modifier
        )

        else -> UnknownOriginUI(modifier) // TODO Add generic matcher info
    }
}

@Composable
fun TwoIdentifierSources(
    identifier1: Identifier<out Any>,
    identifier2: Identifier<out Any>,
    modifier: Modifier = Modifier,
    target1: Identifiable? = null,
    target2: Identifiable? = null
) {
    Row(
        modifier,
        verticalAlignment = Alignment.Bottom
    ) {
        IdentifierUI(identifier1, modifier = Modifier.weight(1f), target1)
        Spacer(modifier = modifier.width(8.dp))
        IdentifierUI(identifier2, modifier = Modifier.weight(1f), target2)
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
