package de.medizininformatikinitiative.medgraph.ui.searchengine.pipeline

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.Judgement
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.ScoredJudgement
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.theme.ApplicationTheme
import de.medizininformatikinitiative.medgraph.ui.theme.Situation
import de.medizininformatikinitiative.medgraph.ui.theme.localColors
import de.medizininformatikinitiative.medgraph.ui.theme.templates.clipToBox


@Composable
@Preview
private fun JudgementDisplay() {
    ApplicationTheme {
        Column {
            JudgementDisplay(
                ScoredJudgement("Sample Judgement", "Always fails", 0.5, 1.0),
                Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
            )
            JudgementDisplay(
                ScoredJudgement(
                    "Other sample Judgement",
                    "Always passes, also I wanna see what happens if this text becomes very long so I write a lot of text here",
                    1.5,
                    1.0
                ),
                Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
            )
        }
    }
}

/**
 * Displays information about a judgement.
 */
@Composable
fun JudgementDisplay(judgement: Judgement, modifier: Modifier = Modifier) {

    val backgroundColor =
        if (judgement.isPassed) MaterialTheme.localColors.weakSuccess else MaterialTheme.localColors.weakFailure
    Column(
        modifier = modifier.clipToBox(color = backgroundColor)
    ) {
        Text(judgement.name, style = MaterialTheme.typography.h5)
        Text(
            judgement.description,
            style = MaterialTheme.typography.subtitle2,
            modifier = Modifier.padding(start = 4.dp)
        )

        val resultText = if (judgement.isPassed) StringRes.judgement_passed else StringRes.judgement_failed

        Row {
            Text(StringRes.judgement_result, style = MaterialTheme.typography.h6)
            if (judgement.isPassed) {
                Text(
                    StringRes.judgement_passed,
                    color = MaterialTheme.localColors.strongSuccess,
                    style = MaterialTheme.typography.h6
                )
            } else {
                Text(
                    StringRes.judgement_failed,
                    color = MaterialTheme.localColors.strongFailure,
                    style = MaterialTheme.typography.h6
                )
            }
        }
    }

}