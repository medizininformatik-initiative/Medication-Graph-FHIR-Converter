package de.medizininformatikinitiative.medgraph.ui.theme.templates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TextBoxes(
    objects: Iterable<*>,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colors.onSurface,
    backgroundColor: Color = MaterialTheme.colors.surface,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp, horizontalAlignment),
        verticalArrangement = Arrangement.spacedBy(4.dp, verticalAlignment),
        modifier = modifier
    ) {
        objects.forEach { obj ->
            TextBox(obj.toString(), textColor = textColor, backgroundColor = backgroundColor)
        }
    }
}

@Composable
fun TextBox(
    text: String,
    textColor: Color = MaterialTheme.colors.onSurface,
    textAlign: TextAlign = TextAlign.Center,
    backgroundColor: Color = MaterialTheme.colors.surface,
    modifier: Modifier = Modifier
) {
    Text(
        text,
        color = textColor,
        textAlign = textAlign,
        modifier = modifier
            .clipToBox(backgroundColor)
    )
}