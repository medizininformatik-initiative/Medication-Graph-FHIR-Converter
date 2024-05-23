package de.medizininformatikinitiative.medgraph.ui.searchengine.pipeline

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.MatchingPipelineStep
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.ScoredJudgement
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.Transformation
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.theme.TUMWeb
import de.medizininformatikinitiative.medgraph.ui.theme.localColors
import de.medizininformatikinitiative.medgraph.ui.theme.templates.clipToBox

@Composable
fun MatchingPipelineStepDisplay(
    step: MatchingPipelineStep,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.surface,
    body: @Composable ColumnScope.() -> Unit
) {

    Column(
        modifier = modifier.clipToBox(backgroundColor)
    ) {
        Text(step.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.h5)
        Text(
            step.description,
            style = MaterialTheme.typography.subtitle2,
            modifier = Modifier.padding(start = 4.dp)
        )

        body()
    }
}