package de.medizininformatikinitiative.medgraph.ui.searchengine.pipeline

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Merge
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes


@Composable
fun MergeDisplay(merge: Merge,
                 modifier: Modifier = Modifier) {
    MatchingPipelineStepDisplay(
        StringRes.pipeline_merge,
        StringRes.get(StringRes.pipeline_merge_desc, merge.sourceObjects.size)
    ) {}
}