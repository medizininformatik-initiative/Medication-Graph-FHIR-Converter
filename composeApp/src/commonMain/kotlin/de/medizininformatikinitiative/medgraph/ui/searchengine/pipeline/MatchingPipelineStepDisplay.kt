package de.medizininformatikinitiative.medgraph.ui.searchengine.pipeline

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.MatchingPipelineStep
import de.medizininformatikinitiative.medgraph.ui.theme.templates.clipToBox

@Composable
fun MatchingPipelineStepDisplay(
    step: MatchingPipelineStep,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.surface,
    body: @Composable ColumnScope.() -> Unit
) = MatchingPipelineStepDisplay(step.name, step.description, modifier, backgroundColor, body)

@Composable
fun MatchingPipelineStepDisplay(
    name: String,
    description: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.surface,
    body: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.clipToBox(backgroundColor)
    ) {
        Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.h5)
        Text(
            description,
            style = MaterialTheme.typography.subtitle2,
            modifier = Modifier.padding(start = 4.dp)
        )

        body()
    }
}