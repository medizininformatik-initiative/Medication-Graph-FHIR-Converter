package de.medizininformatikinitiative.medgraph.ui.tools

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onKeyEvent
import java.awt.event.KeyEvent

actual fun Modifier.captureEnterPress(callback: () -> Unit) = this.onKeyEvent { event ->
    val nativeEvent = event.nativeKeyEvent as KeyEvent
    if (nativeEvent.keyChar == '\n') {
        callback()
        return@onKeyEvent true
    } else {
        return@onKeyEvent false
    }
}