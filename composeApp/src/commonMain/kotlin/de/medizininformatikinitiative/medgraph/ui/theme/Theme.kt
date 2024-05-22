package de.medizininformatikinitiative.medgraph.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

private val LightColorPalette = ThemeColors(
    lightColors(
        primary = CorporateDesign.Main.TUMBlue,
        primaryVariant = CorporateDesign.Secondary.DarkBlue,
        secondary = CorporateDesign.Secondary.LightBlue
    ),
    weakSuccess = TUMWeb.TumGreen2,
    strongSuccess = TUMWeb.TumGreenDark,
    weakFailure = TUMWeb.TumRed2,
    strongFailure = TUMWeb.TumRedDark,
)

private val LocalColors = staticCompositionLocalOf { LightColorPalette }

/**
 * Wrapper which applies the application theme around the given content.
 * @author Markus Budeus
 */
@Composable
fun ApplicationTheme(
    content: @Composable () -> Unit
) {
    val colorPalette = LightColorPalette

    CompositionLocalProvider(LocalColors provides colorPalette) {
        MaterialTheme(
            colors = colorPalette.material,
            content = content
        )
    }
}

val MaterialTheme.localColors: ThemeColors
    @Composable
    @ReadOnlyComposable
    get() = LocalColors.current