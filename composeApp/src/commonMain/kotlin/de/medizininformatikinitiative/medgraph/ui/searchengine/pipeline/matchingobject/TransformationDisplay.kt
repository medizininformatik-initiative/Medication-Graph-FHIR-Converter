package de.medizininformatikinitiative.medgraph.ui.searchengine.pipeline.matchingobject

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Product
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Substance
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.transformer.Transformation
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.searchengine.results.IdentifiableObjectUI
import de.medizininformatikinitiative.medgraph.ui.theme.ApplicationTheme
import de.medizininformatikinitiative.medgraph.ui.theme.localColors
import de.medizininformatikinitiative.medgraph.ui.theme.templates.TextBox

@Composable
@Preview
private fun TransformationDisplay() {
    ApplicationTheme {
        Column {
            TransformationDisplay(
                Transformation(
                    "No output",
                    "This transformation produced nothing.",
                    emptyList()
                ),
                modifier = Modifier.padding(4.dp)
            )
            TransformationDisplay(
                Transformation(
                    "Some output", "This transformation produced a few outputs.", listOf(
                        Product(
                            1,
                            "Prednisolut Pulver"
                        ),
                        Product(
                            2,
                            "Aspirin"
                        ),
                        Substance(
                            5,
                            "Midazolam"
                        )
                    )
                ),
                highlightedOutputIndex = 1,
                modifier = Modifier.fillMaxWidth().padding(4.dp)
            )
        }
    }
}

/**
 * Displays information about a transformation.
 */
@Composable
fun TransformationDisplay(
    transformation: Transformation<*>,
    highlightedOutputIndex: Int? = null,
    modifier: Modifier = Modifier
) {
    MatchingPipelineStepDisplay(transformation, modifier = modifier) {
        Spacer(modifier.height(4.dp))
        val results: List<Matchable> = transformation.result()
        if (results.isEmpty()) {
            TextBox(
                text = StringRes.transformation_no_output,
                textColor = MaterialTheme.localColors.strongFailure,
                backgroundColor = MaterialTheme.localColors.surface2,
                modifier = modifier.fillMaxWidth()
            )
        } else {
            Text(text = StringRes.transformation_outputs, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
            Column(
                modifier = Modifier
                    .border(2.dp, color = MaterialTheme.colors.onBackground, RoundedCornerShape(4.dp))
                    .padding(4.dp)
                    .heightIn(50.dp, 300.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                results.forEachIndexed() { index, result ->
                    IdentifiableObjectUI(
                        result,
                        backgroundColor = if (index == highlightedOutputIndex)
                            MaterialTheme.localColors.surfaceHighlight
                        else MaterialTheme.localColors.surface2,
                        modifier = Modifier.fillMaxWidth()
                            .padding(4.dp)
                    )
                }
            }
        }
    }

}