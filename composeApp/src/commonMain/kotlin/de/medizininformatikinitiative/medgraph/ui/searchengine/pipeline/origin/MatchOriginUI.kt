package de.medizininformatikinitiative.medgraph.ui.searchengine.pipeline.origin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.medizininformatikinitiative.medgraph.searchengine.matcher.EditDistanceListMatcher
import de.medizininformatikinitiative.medgraph.searchengine.matcher.EditDistanceSetMatcher
import de.medizininformatikinitiative.medgraph.searchengine.matcher.IMatcher
import de.medizininformatikinitiative.medgraph.searchengine.matcher.LevenshteinMatcher
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Identifiable
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.Identifier
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchOrigin
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.searchengine.pipeline.identifier.IdentifierUI
import de.medizininformatikinitiative.medgraph.ui.theme.templates.ContentCard
import de.medizininformatikinitiative.medgraph.ui.theme.templates.clipToBox

@Suppress("UNCHECKED_CAST")
@Composable
fun MatchOriginUI(origin: MatchOrigin<*>, modifier: Modifier = Modifier) {

    TwoIdentifierSources(
        origin.match.searchTerm,
        origin.match.matchedIdentifier.identifier,
        target2 = origin.match.matchedIdentifier.target
    )
    when (origin.matcher) {
        is EditDistanceSetMatcher -> EditDistanceSetMatcherUI(
            origin as MatchOrigin<EditDistanceSetMatcher.Match>,
            modifier
        )
        is EditDistanceListMatcher -> EditDistanceListMatcherUI(
            origin as MatchOrigin<out EditDistanceListMatcher.Match>,
            modifier
        )
        else -> GenericMatchOriginUI(origin, modifier, null)
    }
}

@Composable
fun GenericMatchOriginUI(origin: MatchOrigin<*>, modifier: Modifier = Modifier, content: (@Composable BoxScope.() -> Unit)?) {
    ContentCard(
        title = origin.matcher.toString(),
        description = getDescription(origin.matcher.javaClass),
        modifier = modifier
    ) {
        if (content != null) {
            Box(
                modifier = Modifier
                    .clipToBox(color = MaterialTheme.colors.background)
            ) {
                content()
            }
        }

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

fun getDescription(matcher: Class<out IMatcher<*, *, *>>): String {
    return when(matcher) {
        EditDistanceSetMatcher::class.java -> StringRes.edit_distance_set_matcher_description
        LevenshteinMatcher::class.java -> StringRes.levenshtein_matcher_description
        else -> StringRes.unknown_matcher_description
    }
}