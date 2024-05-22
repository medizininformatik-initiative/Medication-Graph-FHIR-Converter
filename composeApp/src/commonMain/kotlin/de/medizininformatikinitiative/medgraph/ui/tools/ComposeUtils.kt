package de.medizininformatikinitiative.medgraph.ui.tools

import androidx.compose.ui.Modifier

/**
 * Registers a key event handler on this modifier which calls the given callback when enter is pressed while this
 * view is focused.
 */
expect fun Modifier.captureEnterPress(callback: () -> Unit): Modifier