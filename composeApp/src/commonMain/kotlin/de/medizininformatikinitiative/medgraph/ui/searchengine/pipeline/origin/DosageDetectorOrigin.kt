package de.medizininformatikinitiative.medgraph.ui.searchengine.pipeline.origin

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.substring
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.OriginalIdentifier
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.DosageDetectorOrigin
import de.medizininformatikinitiative.medgraph.searchengine.tools.DosageDetector
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.searchengine.pipeline.identifier.IdentifierUI
import de.medizininformatikinitiative.medgraph.ui.theme.ApplicationTheme
import de.medizininformatikinitiative.medgraph.ui.theme.localColors
import de.medizininformatikinitiative.medgraph.ui.theme.templates.ContentCard
import de.medizininformatikinitiative.medgraph.ui.theme.templates.TextBox
import de.medizininformatikinitiative.medgraph.ui.theme.templates.clipToBox

@Composable
@Preview
private fun DosageDetectorOriginUI() {
    val input = "Dormicum 15mg/3ml Injektionslsg."
    val dd = DosageDetector.detectDosages(input).get(0)
    ApplicationTheme {
        DosageDetectorOriginUI(
            DosageDetectorOrigin(dd, OriginalIdentifier(input, OriginalIdentifier.Source.RAW_QUERY)),
            Modifier.fillMaxWidth().padding(8.dp)
        )
    }
}

@Composable
fun DosageDetectorOriginUI(origin: DosageDetectorOrigin, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier,
    ) {
        IdentifierUI(origin.identifier)
        ContentCard(
            title = StringRes.dosage_detector_title,
            description = StringRes.dosage_detector_desc,
        ) {
            Column(modifier = Modifier.clipToBox(MaterialTheme.localColors.background)) {

                val identifier = origin.identifier.identifier
                Text(buildAnnotatedString {
                    val startIndex = origin.detectedDosage.startIndex
                    val endIndex = startIndex + origin.detectedDosage.length
                    append(identifier.substring(0, startIndex))
                    withStyle(SpanStyle(MaterialTheme.localColors.highlightDosage)) {
                        append(identifier.substring(startIndex, endIndex))
                    }
                    append(identifier.substring(endIndex))
                }, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    TextBox(
                        origin.detectedDosage.dosage.toString(),
                        textColor = MaterialTheme.localColors.highlightDosage,
                        backgroundColor = MaterialTheme.localColors.surfaceHighlight,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
    }
}