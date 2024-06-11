package de.medizininformatikinitiative.medgraph.ui.theme.templates

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Modifier

/**
 * Preconfigured button for this application.
 */
@Composable
@NonRestartableComposable
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable (RowScope.() -> Unit)
) {
    androidx.compose.material.Button(onClick, modifier = modifier, enabled = enabled, content = content)
}