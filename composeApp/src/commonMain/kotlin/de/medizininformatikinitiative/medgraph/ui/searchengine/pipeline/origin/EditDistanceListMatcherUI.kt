package de.medizininformatikinitiative.medgraph.ui.searchengine.pipeline.origin

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.medizininformatikinitiative.medgraph.common.EDQM
import de.medizininformatikinitiative.medgraph.searchengine.matcher.EditDistanceListMatcher
import de.medizininformatikinitiative.medgraph.searchengine.matcher.editdistance.LevenshteinDistanceService
import de.medizininformatikinitiative.medgraph.searchengine.matcher.model.DetailedMatch
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.EdqmConcept
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.Identifier
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.OriginalIdentifier
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchOrigin
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Origin
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

typealias EditDistanceListMatcherOrigin = MatchOrigin<out DetailedMatch<out Identifier<List<String>>, out Identifier<List<String>>, EditDistanceListMatcher.MatchInfo>>

@Composable
fun EditDistanceListMatcherUI(
    origin: EditDistanceListMatcherOrigin,
    modifier: Modifier = Modifier
) {
    GenericMatchOriginUI(origin, modifier) {
        val match = origin.match
        val matchInfo = match.matchInfo
        val searchTermTokens = match.searchTerm.identifier
        val matchStartPosition = matchInfo.usageStatement.usedIndices.min()
        val matchEndPosition = matchInfo.usageStatement.usedIndices.max()
        if (matchStartPosition == null || matchEndPosition == null) {
            Text(
                "Unexpectedly, the usage statement does not provide any used indices of the search term.",
                color = MaterialTheme.colors.error
            )
            return@GenericMatchOriginUI
        }

        val initial = searchTermTokens.subList(0, matchStartPosition)
        val trailing = searchTermTokens.subList(matchEndPosition + 1, searchTermTokens.size)

        Column {
            EditDistanceTextBoxColumn(initial)
            Divider(modifier = Modifier.fillMaxWidth().padding(4.dp), thickness = 2.dp)
            Text(
                StringRes.get(StringRes.edit_distance, matchInfo.distance.editDistance),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            SideBySideEditDistanceTextBoxes(
                searchTermTokens.subList(matchStartPosition, matchEndPosition + 1),
                match.matchedIdentifier.identifier,
                modifier = Modifier.fillMaxWidth()
            )
            Divider(modifier = Modifier.fillMaxWidth().padding(4.dp), thickness = 2.dp)
            EditDistanceTextBoxColumn(trailing)
        }
    }
}