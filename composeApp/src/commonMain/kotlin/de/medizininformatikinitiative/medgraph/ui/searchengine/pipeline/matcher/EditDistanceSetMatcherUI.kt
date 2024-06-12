package de.medizininformatikinitiative.medgraph.ui.searchengine.pipeline.matcher

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.medizininformatikinitiative.medgraph.searchengine.matcher.EditDistanceSetMatcher
import de.medizininformatikinitiative.medgraph.searchengine.matcher.IMatcher
import de.medizininformatikinitiative.medgraph.searchengine.matcher.editdistance.LevenshteinDistanceService
import de.medizininformatikinitiative.medgraph.searchengine.matcher.model.EditDistance
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Product
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.OriginalIdentifier
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchOrigin
import de.medizininformatikinitiative.medgraph.searchengine.provider.BaseProvider
import de.medizininformatikinitiative.medgraph.searchengine.provider.MappedIdentifier
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.theme.ApplicationTheme
import de.medizininformatikinitiative.medgraph.ui.theme.localColors
import de.medizininformatikinitiative.medgraph.ui.theme.templates.ContentCard
import de.medizininformatikinitiative.medgraph.ui.theme.templates.TextBox
import de.medizininformatikinitiative.medgraph.ui.theme.templates.clipToBox

@Composable
@Preview
internal fun EditDistanceSetMatcherUI() {
    val searchTerm = OriginalIdentifier(setOf("Dormacum", "Midazloam"), OriginalIdentifier.Source.RAW_QUERY);
    val identifiers = BaseProvider.ofIdentifiers(
        setOf(
            MappedIdentifier(
                OriginalIdentifier(
                    setOf("Dormicum", "V", "5mg/ml", "Midazolam"),
                    OriginalIdentifier.Source.KNOWN_IDENTIFIER
                ), Product(1, "Dormicum V 5mg/ml")
            )
        )
    )

    val matcher = EditDistanceSetMatcher(LevenshteinDistanceService(2))
    val match = matcher.match(searchTerm, identifiers).findFirst().get()

    ApplicationTheme {
        EditDistanceSetMatcherUI(MatchOrigin(match, matcher), modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth())
    }
}

@Composable
fun EditDistanceSetMatcherUI(origin: MatchOrigin<out EditDistanceSetMatcher.Match>, modifier: Modifier = Modifier) {
    // TODO Information about the matcher in general

    ContentCard(
        title = origin.matcher.toString(),
        description = getDescription(origin.matcher.javaClass),
        modifier = modifier
    ) {
        val match = origin.match
        Box(
            modifier = Modifier
                .clipToBox(color = MaterialTheme.colors.background)
        ) {
            EditDistancesUI(
                match.editDistances,
                match.searchTerm.identifier,
                match.matchedIdentifier.identifier.identifier,
            )
        }
    }

}

@Composable
fun EditDistancesUI(
    editDistances: Collection<EditDistance>,
    allSearchTerms: Collection<String>,
    allTargetTerms: Collection<String>,
    modifier: Modifier = Modifier
) {
    val remainingSearchTermTokens = ArrayList(allSearchTerms)
    val remainingTargetTokens = ArrayList(allTargetTerms)
    val editDistanceList = ArrayList(editDistances)
    for (editDistance in editDistances) {
        remainingSearchTermTokens.remove(editDistance.value1())
        remainingTargetTokens.remove(editDistance.value2())
    }
    EditDistancesUI(editDistanceList, remainingSearchTermTokens, remainingTargetTokens, modifier)
}

@Composable
fun EditDistancesUI(
    editDistances: List<EditDistance>,
    remainingSearchTerms: List<String>,
    remainingTargetTerms: List<String>,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        for (editDistance in editDistances) {
            EditDistanceDisplay(editDistance, Modifier.fillMaxWidth())
        }

        Row(
            modifier = modifier,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                for (searchTermToken in remainingSearchTerms)
                    EditDistanceTextBox(
                        searchTermToken,
                        modifier = Modifier.align(Alignment.Start)
                    )
            }
            Column(modifier = Modifier.weight(1f)) {
                for (targetToken in remainingTargetTerms)
                    EditDistanceTextBox(
                        targetToken,
                        modifier = Modifier.align(Alignment.End)
                    )
            }
        }
    }
}

@Composable
fun EditDistanceDisplay(editDistance: EditDistance, modifier: Modifier) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        EditDistanceTextBox(editDistance.value1())
        Text(StringRes.get(StringRes.pipeline_origin_edit_distance, editDistance.editDistance()))
        EditDistanceTextBox(editDistance.value2())
    }
}

@Composable
fun EditDistanceTextBox(text: String, modifier: Modifier = Modifier) {
    TextBox(text, backgroundColor = MaterialTheme.localColors.surface2, modifier = modifier.padding(2.dp))
}

fun getDescription(matcher: Class<out IMatcher<*, *, *>>): String {
    return when(matcher) {
        EditDistanceSetMatcher::class.java -> StringRes.edit_distance_set_matcher_description
        else -> ""
    }
}