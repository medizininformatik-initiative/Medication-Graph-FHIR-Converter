package de.medizininformatikinitiative.medgraph.ui.searchengine.pipeline.matchingobject

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.medizininformatikinitiative.medgraph.searchengine.model.ScoreIncorporationStrategy
import de.medizininformatikinitiative.medgraph.searchengine.model.ScoreJudgedObject
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Product
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Substance
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.*
import de.medizininformatikinitiative.medgraph.searchengine.model.pipelinestep.ScoredJudgementStep
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.ScoreJudgeConfiguration
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.judge.ScoreJudgementInfo
import de.medizininformatikinitiative.medgraph.searchengine.pipeline.transformer.Transformation
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.searchengine.pipeline.origin.OriginUI
import de.medizininformatikinitiative.medgraph.ui.searchengine.results.DetailedIdentifiableObjectUI
import de.medizininformatikinitiative.medgraph.ui.theme.ApplicationTheme

@Composable
@Preview
private fun MatchingObjectSourcePipelineDisplay() {
    ApplicationTheme {
        var obj: MatchingObject<*> = OriginalMatch(
            Substance(
                1,
                "THE ORIGIN"
            )
        )
        obj = Merge(
            listOf(
                OriginalMatch(
                    Substance(
                        1,
                        "THE ORIGIN"
                    )
                ), obj
            ),
            ScoreMergingStrategy.MAX
        )
        obj = ScoreJudgedObject(
            obj,
            ScoredJudgementStep(
                "Judgement 1",
                "Assigns a score of 0.5",
                ScoreJudgementInfo(0.5),
                ScoreJudgeConfiguration(1.0, true, 1.0, ScoreIncorporationStrategy.ADD)
            ),
        )
        obj = ScoreJudgedObject(
            obj,
            ScoredJudgementStep(
                "Judgement 2",
                "Assigns a score of 1.5",
                ScoreJudgementInfo(1.5),
                ScoreJudgeConfiguration(1.0, true, 1.5, ScoreIncorporationStrategy.MULTIPLY)
            ),
        )

        val product =
            Product(
                2,
                "THE ACTUAL PRODUCT"
            )
        val transformation =
            Transformation(
                "Transformation X",
                "Here, the object was transformed",
                listOf(
                    product,
                    Product(
                        3,
                        "Noone cares about this output"
                    )
                )
            )
        obj = TransformedObject(product, obj, transformation)

        obj = ScoreJudgedObject(
            obj,
            ScoredJudgementStep(
                "Wrath of god",
                "Barely made it out alive",
                ScoreJudgementInfo(1.0),
                ScoreJudgeConfiguration(1.0, true, 42.0, ScoreIncorporationStrategy.ADD)
            ),
        )

        MatchingObjectPipelineDisplay(obj, Modifier.padding(4.dp))
    }
}

@Composable
fun MatchingObjectPipelineDisplay(obj: MatchingObject<*>, modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
    ) {
        MatchingObjectDisplay(obj, scrollState)
    }
}

@Composable
private fun MatchingObjectDisplay(obj: MatchingObject<*>, scrollState: ScrollState) {
    when (obj) {
        is OriginalMatch -> OriginalMatchDisplay(obj)
        is Merge -> MergedObjectsDisplay(obj, scrollState)
        is TransformedObject<*, *> -> TransformedObjectDisplay(obj, scrollState)
        is JudgedObject -> JudgedObjectDisplay(obj, scrollState)
        else -> Text("Unknown object type encountered!")
    }
}

@Composable
private fun OriginalMatchDisplay(obj: OriginalMatch<*>) {
    OriginUI(obj.origin)
    MatchingObjectWithScoreSplitScreen(obj) {
        DetailedIdentifiableObjectUI(obj.`object`, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun JudgedObjectDisplay(obj: JudgedObject<*>, scrollState: ScrollState) {
    MatchingObjectDisplay(obj.source, scrollState)
    MatchingObjectWithScoreSplitScreen(
        obj,
        mainBody = {
            JudgementDisplay(obj.judgement, modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp))
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun MergedObjectsDisplay(obj: Merge<*>, scrollState: ScrollState) {
    var currentSelectionIndex by remember { mutableStateOf(0) }
    currentSelectionIndex = Math.max(0, Math.min(obj.sourceObjects.size - 1, currentSelectionIndex))
    MatchingObjectDisplay(obj.sourceObjects[currentSelectionIndex], scrollState)
    MatchingObjectWithScoreSplitScreen(obj) {
        MergeDisplay(
            obj, currentSelectionIndex, onSelectSourcePath = { pathIndex -> currentSelectionIndex = pathIndex },
            pipelineScrollState = scrollState
        )
    }
}

@Composable
private fun TransformedObjectDisplay(obj: TransformedObject<*, *>, scrollState: ScrollState) {
    MatchingObjectDisplay(obj.source, scrollState)
    val targetObjectIndex = obj.transformation.result().indexOf(obj.`object`)
    MatchingObjectWithScoreSplitScreen(obj) {
        TransformationDisplay(obj.transformation, highlightedOutputIndex = targetObjectIndex)
    }
}

@Composable
private fun MatchingObjectWithScoreSplitScreen(
    obj: MatchingObject<*>,
    modifier: Modifier = Modifier.fillMaxWidth(),
    mainBody: @Composable ColumnScope.() -> Unit = {},
) {
    Row(modifier.height(IntrinsicSize.Min)) {
        Column(modifier = Modifier.weight(1f)) {
            mainBody()
        }
        Spacer(Modifier.width(4.dp))
        Column(
            modifier = Modifier.width(70.dp).fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(StringRes.matching_object_score, fontWeight = FontWeight.Bold)
            Text(StringRes.formatDecimal(obj.score), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.h5)
        }
    }
}
