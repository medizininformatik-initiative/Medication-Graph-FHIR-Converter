package de.medizininformatikinitiative.medgraph.ui.searchengine.pipeline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Identifiable
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.Identifier
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject
import de.medizininformatikinitiative.medgraph.searchengine.provider.MappedIdentifier
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.searchengine.pipeline.identifier.IdentifierUI
import de.medizininformatikinitiative.medgraph.ui.searchengine.pipeline.matchingobject.MatchingObjectPipelineDisplay

@Composable
@NonRestartableComposable
fun PipelineInfoDialog(identifier: MappedIdentifier<*>, onDismissRequest: () -> Unit) {
    PipelineInfoDialog(identifier.identifier, onDismissRequest, identifier.target)
}

@Composable
fun PipelineInfoDialog(identifier: Identifier<*>, onDismissRequest: () -> Unit, identifierTarget: Identifiable? = null) {
    PipelineInfoDialog(onDismissRequest) {
        IdentifierUI(identifier, modifier = Modifier.fillMaxWidth(), identifierTarget)
    }
}

@Composable
fun PipelineInfoDialog(target: MatchingObject<*>, onDismissRequest: () -> Unit) {
    PipelineInfoDialog(onDismissRequest) {
        MatchingObjectPipelineDisplay(target, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun PipelineInfoDialog(onDismissRequest: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Dialog(onDismissRequest, DialogProperties(
        dismissOnBackPress = true,
        dismissOnClickOutside = true,
        usePlatformDefaultWidth = true
    )
    ) {
        Column(
            modifier = Modifier
                .width(800.dp)
                .background(MaterialTheme.colors.background, shape = RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Text(StringRes.pipeline_info_title, textAlign = TextAlign.Center, style = MaterialTheme.typography.h4)
            Text(StringRes.pipeline_info_subtitle, style = MaterialTheme.typography.subtitle1)
            Divider(modifier = Modifier.fillMaxWidth(), thickness = 2.dp)
            Spacer(modifier = Modifier.height(4.dp))
            content()
        }

    }
}