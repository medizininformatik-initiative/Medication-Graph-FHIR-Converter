package de.medizininformatikinitiative.medgraph.ui.searchengine.pipeline.matchingobject

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.FilteringStep
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.Judgement
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.ScoredJudgementStep
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.FilteringInfo
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.ScoreJudgeConfiguration
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.ScoreJudgementInfo
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.theme.ApplicationTheme
import de.medizininformatikinitiative.medgraph.ui.theme.localColors


@Composable
@Preview
private fun JudgementDisplay() {
    ApplicationTheme {
        Column {
            JudgementDisplay(
                ScoredJudgementStep(
                    "Sample Judgement",
                    "Always fails",
                    ScoreJudgementInfo(1.0),
                    ScoreJudgeConfiguration(2.0, true)
                ),
                Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
            )
            JudgementDisplay(
                ScoredJudgementStep(
                    "Other sample Judgement",
                    "Always passes, also I wanna see what happens if this text becomes very long so I write a lot of text here",
                    ScoreJudgementInfo(1.5),
                    ScoreJudgeConfiguration()
                ),
                Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
            )
            JudgementDisplay(
                FilteringStep(
                    "Heavenly Filter",
                    "Passes!",
                    FilteringInfo(true),
                ),
                Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
            )
        }
    }
}

/**
 * Displays information about a judgement. If it's a [ScoredJudgementStep], also displays the score and the passing score.
 */
@Composable
fun JudgementDisplay(judgement: Judgement, modifier: Modifier = Modifier) {

    val passingBackgroundColor =
        if (judgement.passed()) MaterialTheme.localColors.weakSuccess else MaterialTheme.localColors.weakFailure

    val hasPassingBorder = !(judgement is ScoredJudgementStep && judgement.configuration.passingScore == null)

    MatchingPipelineStepDisplay(
        judgement,
        modifier,
        if (hasPassingBorder) passingBackgroundColor else MaterialTheme.localColors.surface,
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (judgement is ScoredJudgementStep) {

                Column(horizontalAlignment = Alignment.Start) {
                    val passingScore = judgement.configuration.passingScore
                    val text: String;
                    if (passingScore != null) {
                        text = StringRes.get(
                            StringRes.judgement_score_with_passing_score,
                            StringRes.formatDecimal(judgement.score),
                            StringRes.formatDecimal(passingScore)
                        )
                    } else {
                        text = StringRes.get(StringRes.judgement_score, StringRes.formatDecimal(judgement.score))
                    }
                    Text(text, style = MaterialTheme.typography.h6)

                    Text(
                        StringRes.get(
                            StringRes.judgement_weight,
                            StringRes.formatDecimal(judgement.configuration.scoreWeight)
                        ),
                        style = MaterialTheme.typography.h6
                    )
                }
            }

            if (hasPassingBorder) {
                PassedOrFailedText(judgement.passed(), Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun PassedOrFailedText(passed: Boolean, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.End,
        modifier = modifier
    ) {
        Text(StringRes.judgement_result, style = MaterialTheme.typography.h6)
        if (passed) {
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