package de.medizininformatikinitiative.medgraph.ui.searchengine.pipeline.origin

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.medizininformatikinitiative.medgraph.searchengine.matcher.EditDistanceSetMatcher
import de.medizininformatikinitiative.medgraph.searchengine.matcher.editdistance.LevenshteinDistanceService
import de.medizininformatikinitiative.medgraph.searchengine.matcher.model.DetailedMatch
import de.medizininformatikinitiative.medgraph.searchengine.matcher.model.EditDistance
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Product
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.Identifier
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.OriginalIdentifier
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchOrigin
import de.medizininformatikinitiative.medgraph.searchengine.provider.BaseProvider
import de.medizininformatikinitiative.medgraph.searchengine.provider.MappedIdentifier
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.theme.ApplicationTheme
import de.medizininformatikinitiative.medgraph.ui.theme.localColors
import de.medizininformatikinitiative.medgraph.ui.theme.templates.TextBox

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
        EditDistanceSetMatcherUI(
            MatchOrigin(match, matcher), modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        )
    }
}

typealias EditDistanceSetMatcherOrigin =  MatchOrigin<out DetailedMatch<out Identifier<Set<String>>, out Identifier<Set<String>>, EditDistanceSetMatcher.MatchInfo>>

@Composable
fun EditDistanceSetMatcherUI(origin: EditDistanceSetMatcherOrigin, modifier: Modifier = Modifier) {
    GenericMatchOriginUI(origin, modifier) {
        val match = origin.match
        EditDistancesUI(
            match.matchInfo.editDistances,
            match.searchTerm.identifier,
            match.matchedIdentifier.identifier,
        )
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
        SideBySideEditDistanceTextBoxes(remainingSearchTerms, remainingTargetTerms, Modifier.fillMaxWidth(), Alignment.Top)
    }
}

@Composable
fun SideBySideEditDistanceTextBoxes(
    left: List<String>,
    right: List<String>,
    modifier: Modifier = Modifier,
    verticalAlignment: Alignment.Vertical = Alignment.Top
) {
    Row(
        modifier,
        verticalAlignment = verticalAlignment
    ) {
        EditDistanceTextBoxColumn(left, modifier = Modifier.weight(1f), Alignment.Start)
        EditDistanceTextBoxColumn(right, modifier = Modifier.weight(1f), Alignment.End)
    }
}

@Composable
fun EditDistanceTextBoxColumn(tokens: List<String>, modifier: Modifier = Modifier, textBoxAlignment: Alignment.Horizontal = Alignment.Start) {
    Column(modifier = modifier) {
        for (targetToken in tokens)
            EditDistanceTextBox(
                targetToken,
                modifier = Modifier.align(textBoxAlignment)
            )
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