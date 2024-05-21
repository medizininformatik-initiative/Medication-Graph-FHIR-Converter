package de.medizininformatikinitiative.medgraph.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val LightColorPalette = lightColors(
    primary = CorporateDesign.Main.TUMBlue,
    primaryVariant = CorporateDesign.Secondary.DarkBlue,
    secondary = CorporateDesign.Secondary.LightBlue
)

/**
 * Wrapper which applies the application theme around the given content.
 * @author Markus Budeus
 */
@Composable
fun ApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = LightColorPalette,
        content = content
    )
}