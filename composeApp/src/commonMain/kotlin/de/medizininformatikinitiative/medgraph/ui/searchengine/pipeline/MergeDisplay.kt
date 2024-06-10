package de.medizininformatikinitiative.medgraph.ui.searchengine.pipeline

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.dp
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Merge
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun MergeDisplay(merge: Merge,
                 currentSelection: Int = 0,
                 onSelectSourcePath: (Int) -> Unit,
                 pipelineScrollState: ScrollState,
                 modifier: Modifier = Modifier) {
    var lastGlobalPosition by remember { mutableStateOf(0f) }
    var scrollStateDirty by remember { mutableStateOf(false) }
    val selectSourcePath = { path: Int -> scrollStateDirty = true; onSelectSourcePath(path)}
    val coroutineScope = rememberCoroutineScope()
    MatchingPipelineStepDisplay(
        StringRes.pipeline_merge,
        StringRes.get(StringRes.pipeline_merge_desc, merge.sourceObjects.size),
        modifier.onGloballyPositioned { coordinates ->
            val position = coordinates.positionInRoot().y
            if (scrollStateDirty) {
                scrollStateDirty = false
                val offset = position - lastGlobalPosition
                coroutineScope.launch {
                    pipelineScrollState.scrollTo(pipelineScrollState.value + offset.roundToInt())
                }
            }
            lastGlobalPosition = position
        }
    ) {
        val max = merge.sourceObjects.size - 1

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button({ selectSourcePath(0) },
                enabled = currentSelection > 0) {
                Text(StringRes.pipeline_merge_first_path)
            }

            Button({ selectSourcePath(currentSelection - 1) },
                enabled = currentSelection > 0) {
                Text(StringRes.pipeline_merge_previous_path)
            }

            Text(StringRes.get(StringRes.pipeline_merge_current_path, currentSelection + 1))

            Button({ selectSourcePath(currentSelection + 1) },
                enabled = currentSelection < max) {
                Text(StringRes.pipeline_merge_next_path)
            }

            Button({ selectSourcePath(max) },
                enabled = currentSelection < max) {
                Text(StringRes.pipeline_merge_last_path)
            }
        }
    }
}