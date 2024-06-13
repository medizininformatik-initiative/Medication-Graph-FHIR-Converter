package de.medizininformatikinitiative.medgraph.ui.theme.templates

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Displays text boxes in a [FlowRow].
 *
 * @param objects the objects for each of which to display a text box
 * @param modifier the modifier to apply to the [FlowRow]
 * @param textColor the text color to apply to the text boxes
 * @param backgroundColor the background color of the text boxes
 * @param horizontalAlignment the horizontal alignment of the text boxes within the layout
 * @param verticalAlignment the vertical alignment of the text boxes within the layout
 * @param displayNameExtractor a function which extracts the string to display on the checkbox per object
 * @param onClick if not null, the text boxes become clickable and invoke this function as callback
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> TextBoxes(
    objects: Iterable<T>,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colors.onSurface,
    backgroundColor: Color = MaterialTheme.colors.surface,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    displayNameExtractor: (T) -> String = Any?::toString,
    onClick: ((T) -> Unit)? = null,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp, horizontalAlignment),
        verticalArrangement = Arrangement.spacedBy(4.dp, verticalAlignment),
        modifier = modifier
    ) {
        objects.forEach { obj ->
            TextBox(
                displayNameExtractor(obj),
                textColor = textColor,
                backgroundColor = backgroundColor,
                onClick = if (onClick == null) null else ({ onClick(obj) })
            )
        }
    }
}

@Composable
fun TextBox(
    text: String,
    textColor: Color = MaterialTheme.colors.onSurface,
    textAlign: TextAlign = TextAlign.Center,
    backgroundColor: Color = MaterialTheme.colors.surface,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    var mod = modifier.clipToBox(backgroundColor)
    if (onClick != null) {
        val source = remember { MutableInteractionSource() }
        mod = mod.clickable(source, LocalIndication.current) { onClick() }
    }
    Text(
        text,
        color = textColor,
        textAlign = textAlign,
        modifier = mod
    )
}