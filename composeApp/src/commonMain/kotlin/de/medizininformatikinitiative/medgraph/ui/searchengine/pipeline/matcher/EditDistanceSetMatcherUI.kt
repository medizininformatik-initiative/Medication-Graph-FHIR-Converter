package de.medizininformatikinitiative.medgraph.ui.searchengine.pipeline.matcher

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.medizininformatikinitiative.medgraph.searchengine.matcher.EditDistanceSetMatcher
import de.medizininformatikinitiative.medgraph.searchengine.matcher.editdistance.LevenshteinDistanceService
import de.medizininformatikinitiative.medgraph.searchengine.matcher.model.EditDistance
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Product
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.OriginalIdentifier
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchOrigin
import de.medizininformatikinitiative.medgraph.searchengine.provider.BaseProvider
import de.medizininformatikinitiative.medgraph.searchengine.provider.MappedIdentifier
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.searchengine.query.TextBox
import de.medizininformatikinitiative.medgraph.ui.theme.ApplicationTheme

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
        EditDistanceSetMatcherUI(MatchOrigin(match), modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun EditDistanceSetMatcherUI(origin: MatchOrigin<out EditDistanceSetMatcher.Match>, modifier: Modifier = Modifier) {
    // TODO Information about the matcher in general

    val match = origin.match
    EditDistanceMatcherUI(
        match.editDistances,
        match.searchTerm.identifier,
        match.matchedIdentifier.identifier.identifier,
        modifier
    )
}

@Composable
fun EditDistanceMatcherUI(
    editDistances: Collection<EditDistance>,
    allSearchTerms: Collection<String>,
    allTargetTerms: Collection<String>,
    modifier: Modifier = Modifier
) {
    val remainingSearchTermTokens = ArrayList(allSearchTerms)
    val remainingTargetTokens = ArrayList(allTargetTerms)
    val editDistanceList = ArrayList(editDistances)
    for (editDistance in editDistances) {
        remainingSearchTermTokens.remove(editDistance.value1)
        remainingTargetTokens.remove(editDistance.value2)
    }
    EditDistanceMatcherUI(editDistanceList, remainingSearchTermTokens, remainingTargetTokens, modifier)
}

@Composable
fun EditDistanceMatcherUI(
    editDistances: List<EditDistance>,
    remainingSearchTerms: List<String>,
    remainingTargetTerms: List<String>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            for (editDistance in editDistances) {
                EditDistanceTextBox(
                    editDistance.value1,
                    modifier = Modifier.align(Alignment.Start)
                )
            }
            for (searchTermToken in remainingSearchTerms)
                EditDistanceTextBox(
                    searchTermToken,
                    modifier = Modifier.align(Alignment.Start)
                )
        }
        Column {
            for (editDistance in editDistances) {
                Text(StringRes.get(StringRes.pipeline_origin_edit_distance, editDistance.editDistance))
            }
        }
        Column {
            for (editDistance in editDistances) {
                EditDistanceTextBox(
                    editDistance.value2,
                    modifier = Modifier.align(Alignment.Start)
                )
            }
            for (targetToken in remainingTargetTerms)
                EditDistanceTextBox(
                    targetToken,
                    modifier = Modifier.align(Alignment.End)
                )
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
        EditDistanceTextBox(editDistance.value1)
        Text(StringRes.get(StringRes.pipeline_origin_edit_distance, editDistance.editDistance))
        EditDistanceTextBox(editDistance.value2)
    }
}

@Composable
fun EditDistanceTextBox(text: String, modifier: Modifier = Modifier) {
    TextBox(text, modifier = modifier.padding(2.dp))
}