package de.medizininformatikinitiative.medgraph.ui.searchengine.pipeline.origin

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.medizininformatikinitiative.medgraph.searchengine.matcher.EditDistanceListMatcher
import de.medizininformatikinitiative.medgraph.searchengine.matcher.EditDistanceSetMatcher
import de.medizininformatikinitiative.medgraph.searchengine.matcher.IMatcher
import de.medizininformatikinitiative.medgraph.searchengine.matcher.LevenshteinMatcher
import de.medizininformatikinitiative.medgraph.searchengine.matcher.model.DetailedMatch
import de.medizininformatikinitiative.medgraph.searchengine.matcher.model.Match
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Identifiable
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.Identifier
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.TrackableIdentifier
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchOrigin
import de.medizininformatikinitiative.medgraph.searchengine.provider.MappedIdentifier
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.searchengine.pipeline.identifier.IdentifierUI
import de.medizininformatikinitiative.medgraph.ui.theme.templates.ContentCard
import de.medizininformatikinitiative.medgraph.ui.theme.templates.clipToBox

@Suppress("UNCHECKED_CAST")
@Composable
fun MatchOriginUI(origin: MatchOrigin<*>, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        TwoIdentifierSources(
            origin.match.searchTerm,
            origin.match.matchedIdentifier
        )
        when (origin.matcher) {
            is EditDistanceSetMatcher -> EditDistanceSetMatcherUI(
                origin as EditDistanceSetMatcherOrigin,
                Modifier.fillMaxWidth()
            )

            is EditDistanceListMatcher -> EditDistanceListMatcherUI(
                origin as EditDistanceListMatcherOrigin,
                Modifier.fillMaxWidth()
            )

            else -> GenericMatchOriginUI(origin, Modifier.fillMaxWidth(), null)
        }
    }
}

@Composable
fun GenericMatchOriginUI(
    origin: MatchOrigin<*>,
    modifier: Modifier = Modifier,
    content: (@Composable BoxScope.() -> Unit)?
) {
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
    modifier: Modifier = Modifier
) {
    Row(
        modifier,
        verticalAlignment = Alignment.Bottom
    ) {
        IdentifierUI(identifier1, modifier = Modifier.weight(1f))
        Spacer(modifier = modifier.width(8.dp))
        IdentifierUI(identifier2, modifier = Modifier.weight(1f))
    }
}

fun getDescription(matcher: Class<out IMatcher<*, *>>): String {
    return when (matcher) {
        EditDistanceSetMatcher::class.java -> StringRes.edit_distance_set_matcher_description
        LevenshteinMatcher::class.java -> StringRes.levenshtein_matcher_description
        else -> StringRes.unknown_matcher_description
    }
}