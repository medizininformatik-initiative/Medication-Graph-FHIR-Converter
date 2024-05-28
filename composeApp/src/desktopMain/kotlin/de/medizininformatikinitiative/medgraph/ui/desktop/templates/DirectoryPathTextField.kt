package de.medizininformatikinitiative.medgraph.ui.desktop.templates

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.theme.templates.Button
import de.medizininformatikinitiative.medgraph.ui.theme.templates.TextField
import java.io.File
import javax.swing.JFileChooser

@Composable
fun DirectoryPathTextField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    label: String,
    modifier: Modifier = Modifier,
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        TextField(
            value,
            onValueChange,
            modifier = Modifier.weight(1f),
            enabled = enabled,
            label = label
        )
        Button(
            onClick = {
                // This is a hack which completely blocks the UI thread. Not nice, but compose offers no simple file
                // chooser unfortunately
                val fc = JFileChooser()
                fc.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                val initialPath = if (value.isBlank()) System.getProperty("user.dir") else value
                fc.currentDirectory = File(initialPath)
                val returnVal = fc.showOpenDialog(null)
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    onValueChange(fc.selectedFile.absolutePath)
                }
            },
            enabled = enabled,
            modifier = Modifier.padding(4.dp)
        ) {
            Text(StringRes.browse)
        }
    }
}