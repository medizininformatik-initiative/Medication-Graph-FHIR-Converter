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
import javax.swing.filechooser.FileFilter

/**
 * Text field meant to be used for a path string. Includes a browse button.
 *
 * @param value the current value of the text field
 * @param onValueChange callback to be invoked when the value changes
 * @param enabled whether the text field and button are enabled
 * @param label the text field's label
 * @param modifier the modifier to apply
 * @param fileSelectionMode the file selection mode to be used by the file chooser opened by the browse button, as
 * defined by the [JFileChooser]
 * @param fileFilter the file filter to use
 */
@Composable
fun PathTextField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    label: String,
    modifier: Modifier = Modifier,
    fileSelectionMode: Int = JFileChooser.FILES_AND_DIRECTORIES,
    fileFilter: FileFilter? = null,
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
                fc.fileSelectionMode = fileSelectionMode
                val initialPath = if (value.isBlank()) System.getProperty("user.dir") else value
                if (fileFilter != null)
                    fc.fileFilter = fileFilter
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