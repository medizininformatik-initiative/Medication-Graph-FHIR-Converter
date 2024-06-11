package de.medizininformatikinitiative.medgraph.ui.theme.templates

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.medizininformatikinitiative.medgraph.ui.theme.ApplicationTheme
import de.medizininformatikinitiative.medgraph.ui.theme.localColors

@Composable
@Preview
internal fun ContentCard() {
    ApplicationTheme {
        ContentCard(
            title = "Content Card",
            description = "Displays content",
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            TextBoxes(listOf("Text", "Boxes", "for", "Example"), backgroundColor = MaterialTheme.localColors.surface2)
        }
    }
}

@Composable
fun ContentCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    description: String? = null,
    backgroundColor: Color = MaterialTheme.colors.surface,
    content: @Composable ColumnScope.() -> Unit
) {

    Column(
        modifier = modifier
            .clipToBox(backgroundColor)
    ) {
        if (title != null) {
            Text(title, style = MaterialTheme.typography.h5)
        }
        if (description != null) {
            Text(description)
        }

        Column(modifier = Modifier
            .padding(4.dp)) {
            content()
        }
    }

}