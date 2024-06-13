package de.medizininformatikinitiative.medgraph.ui.searchengine.pipeline.origin

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.medizininformatikinitiative.medgraph.common.EDQM
import de.medizininformatikinitiative.medgraph.searchengine.matcher.EditDistanceListMatcher
import de.medizininformatikinitiative.medgraph.searchengine.matcher.editdistance.LevenshteinDistanceService
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.EdqmConcept
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.OriginalIdentifier
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchOrigin
import de.medizininformatikinitiative.medgraph.searchengine.provider.BaseProvider
import de.medizininformatikinitiative.medgraph.searchengine.provider.MappedIdentifier
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.theme.ApplicationTheme

@Composable
@Preview
internal fun EditDistanceListMatcherUI() {
    val searchTerm = OriginalIdentifier(
        listOf("Prednisolon", "Abacus", "Susp.", "zum", "Einnehmen", "100mg"),
        OriginalIdentifier.Source.RAW_QUERY
    );
    val identifiers = BaseProvider.ofIdentifiers(
        setOf(
            MappedIdentifier(
                OriginalIdentifier(
                    listOf("Susp.", "zum", "Einnehmen"),
                    OriginalIdentifier.Source.KNOWN_IDENTIFIER
                ), EdqmConcept("BDF-1111", "Oral Suspension", EDQM.BASIC_DOSE_FORM)
            )
        )
    )

    val matcher = EditDistanceListMatcher(LevenshteinDistanceService(2))
    val match = matcher.match(searchTerm, identifiers).findFirst().get()

    ApplicationTheme {
        EditDistanceListMatcherUI(
            MatchOrigin(match, matcher), modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        )
    }
}

@Composable
fun EditDistanceListMatcherUI(origin: MatchOrigin<out EditDistanceListMatcher.Match>, modifier: Modifier = Modifier) {
    GenericMatchOriginUI(origin, modifier) {
        val match = origin.match
        val searchTermTokens = match.searchTerm.identifier
        val matchStartPosition = match.usageStatement.usedIndices.min()
        val matchEndPosition = match.usageStatement.usedIndices.max()
        if (matchStartPosition == null || matchEndPosition == null) {
            Text("Unexpectedly, the usage statement does not provide any used indices of the search term.",
                color = MaterialTheme.colors.error)
            return@GenericMatchOriginUI
        }

        val initial = searchTermTokens.subList(0, matchStartPosition)
        val trailing = searchTermTokens.subList(matchEndPosition + 1, searchTermTokens.size)

        Column {
            EditDistanceTextBoxColumn(initial)
            Divider(modifier = Modifier.fillMaxWidth().padding(4.dp), thickness = 2.dp)
            Text(StringRes.get(StringRes.edit_distance, match.distance.editDistance), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            SideBySideEditDistanceTextBoxes(
                searchTermTokens.subList(matchStartPosition, matchEndPosition + 1),
                match.matchedIdentifier.identifier.identifier,
                modifier = Modifier.fillMaxWidth()
            )
            Divider(modifier = Modifier.fillMaxWidth().padding(4.dp), thickness = 2.dp)
            EditDistanceTextBoxColumn(trailing)
        }
    }
}