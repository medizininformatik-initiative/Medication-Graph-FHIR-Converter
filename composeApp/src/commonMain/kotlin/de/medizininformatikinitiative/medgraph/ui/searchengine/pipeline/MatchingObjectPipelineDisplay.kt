package de.medizininformatikinitiative.medgraph.ui.searchengine.pipeline

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.*
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.ScoredJudgement
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.Transformation
import de.medizininformatikinitiative.medgraph.ui.searchengine.DetailedMatchableObjectUI
import de.medizininformatikinitiative.medgraph.ui.searchengine.MatchableObjectUI
import de.medizininformatikinitiative.medgraph.ui.theme.ApplicationTheme

@Composable
@Preview
private fun MatchingObjectSourcePipelineDisplay() {
    ApplicationTheme {
        var obj: MatchingObject = OriginalMatch(Substance(1, "THE ORIGIN"))
        obj = Merge(listOf(OriginalMatch(Substance(1, "THE ORIGIN")), obj))
        obj.addJudgement(ScoredJudgement("Judgement 1", "Assigns a score of 0.5", 0.5, 1.0))
        obj.addJudgement(ScoredJudgement("Judgement 2", "Assigns a score of 1.5", 1.5, 1.0))

        val product = Product(2, "THE ACTUAL PRODUCT")
        val transformation = Transformation(
            "Transformation X",
            "Here, the object was transformed",
            listOf(product, Product(3, "Noone cares about this output"))
        )
        obj = TransformedObject(product, obj, transformation)

        obj.addJudgement(ScoredJudgement("Wrath of god", "Barely made it out alive", 1.0, 1.0))

        MatchingObjectPipelineDisplay(obj, Modifier.padding(4.dp))
    }
}

@Composable
fun MatchingObjectPipelineDisplay(obj: MatchingObject, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        MatchingObjectDisplay(obj)
    }
}

@Composable
private fun MatchingObjectDisplay(obj: MatchingObject) {
    when (obj) {
        is OriginalMatch -> OriginalMatchDisplay(obj)
        is Merge -> MergedObjectsDisplay(obj)
        is TransformedObject -> TransformedObjectDisplay(obj)
        else -> Text("Unknown object type encountered!")
    }
    obj.appliedJudgements.forEach {
        JudgementDisplay(it, modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 4.dp))
    }
}

@Composable
private fun OriginalMatchDisplay(obj: OriginalMatch) {
    DetailedMatchableObjectUI(obj.`object`, modifier = Modifier.fillMaxWidth())
}

@Composable
private fun MergedObjectsDisplay(obj: Merge) {
    MatchingObjectDisplay(obj.sourceObjects.get(0))
    MergeDisplay(obj)
}

@Composable
private fun TransformedObjectDisplay(obj: TransformedObject) {
    MatchingObjectDisplay(obj.sourceObject)
    val targetObjectIndex = obj.transformation.result.indexOf(obj.`object`)
    TransformationDisplay(obj.transformation, highlightedOutputIndex = targetObjectIndex)
}
