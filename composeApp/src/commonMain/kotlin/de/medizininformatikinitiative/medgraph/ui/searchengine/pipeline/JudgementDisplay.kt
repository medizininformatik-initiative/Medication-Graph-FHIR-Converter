package de.medizininformatikinitiative.medgraph.ui.searchengine.pipeline

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.Judgement
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.ScoredJudgement
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.theme.ApplicationTheme
import de.medizininformatikinitiative.medgraph.ui.theme.localColors


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
 * Displays information about a judgement. If it's a [ScoredJudgement], also displays the score and the passing score.
 */
@Composable
fun JudgementDisplay(judgement: Judgement, modifier: Modifier = Modifier) {

    val backgroundColor =
        if (judgement.passed()) MaterialTheme.localColors.weakSuccess else MaterialTheme.localColors.weakFailure

    MatchingPipelineStepDisplay(
        judgement,
        modifier,
        backgroundColor,
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (judgement is ScoredJudgement) {
                val passingScore = judgement.passingScore
                val text: String;
                if (passingScore.isPresent) {
                    text = StringRes.get(
                        StringRes.judgement_score_with_passing_score,
                        StringRes.parseDecimal(judgement.score),
                        StringRes.parseDecimal(passingScore.asDouble)
                    )
                } else {
                    text = StringRes.get(StringRes.judgement_score, StringRes.parseDecimal(judgement.score))
                }
                Text(text, style = MaterialTheme.typography.h6)
            }

            Row {
                Text(StringRes.judgement_result, style = MaterialTheme.typography.h6)
                if (judgement.passed()) {
                    Text(
                        StringRes.judgement_passed,
                        color = MaterialTheme.localColors.strongSuccess,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.h6
                    )
                } else {
                    Text(
                        StringRes.judgement_failed,
                        color = MaterialTheme.localColors.strongFailure,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.h6
                    )
                }
            }
        }
    }
}