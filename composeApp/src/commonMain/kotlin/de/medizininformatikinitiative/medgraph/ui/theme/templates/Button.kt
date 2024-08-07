package de.medizininformatikinitiative.medgraph.ui.theme.templates

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
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
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    content: @Composable (RowScope.() -> Unit)
) {
    androidx.compose.material.Button(onClick, modifier = modifier, enabled = enabled, colors = colors, content = content)
}