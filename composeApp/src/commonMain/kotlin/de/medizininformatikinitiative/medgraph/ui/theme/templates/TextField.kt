package de.medizininformatikinitiative.medgraph.ui.theme.templates

import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier


/**
 * Wrapper for Text Field composables used in this application.
 *
 * @param state the MutableState to bind this text field to
 * @param modifier the modifier to apply
 * @param label the label to assign to the text field
 */
@Composable
fun TextField(
    state: MutableState<String>,
    modifier: Modifier = Modifier,
    label: String? = null
) {
    val labelComposable: (@Composable () -> Unit)?
    if (label != null) {
        labelComposable = { Text(label) }
    } else {
        labelComposable = null
    }
    TextField(state.value, { v -> state.value = v }, modifier, label = labelComposable)
}

/**
 * Wrapper for Text Field composables used in this application.
 *
 * @param value the text to assign to the text field
 * @param onValueChange the callback to invoke when the value changes
 * @param modifier the modifier to apply
 * @param label the label to assign to the text field
 */
@Composable
fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable() (() -> Unit)? = null
) {
    OutlinedTextField(value, onValueChange, modifier, label = label)
}
