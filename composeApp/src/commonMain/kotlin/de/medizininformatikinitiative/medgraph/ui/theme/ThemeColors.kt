package de.medizininformatikinitiative.medgraph.ui.theme

import androidx.compose.material.Colors
import androidx.compose.ui.graphics.Color

/**
 * @author Markus Budeus
 */
data class ThemeColors(
    /**
     * Reference to the default MaterialTheme colors.
     */
    val material: Colors,
    /**
     * Weak color used to indicate a success or something good, usable for backgrounds.
     */
    val weakSuccess: Color,
    /**
     * Strong color used to indicate a success or something good, meant to be used in the foreground.
     */
    val strongSuccess: Color,
    /**
     * Weak color used to indicate a failure or something bad, usable for backgrounds.
     */
    val weakFailure: Color,
    /**
     * Strong color used to indicate a failure or something bad, meant to be used in the foreground.
     */
    val strongFailure: Color,
    /**
     * Color to be used for surfaces (i.e. as background) of objects which are to be highlighted.
     */
    val surfaceHighlight: Color,
    /**
     * Color to be used for surfaces on top of a different surface.
     */
    val surface2: Color,
    /**
     * Color to be used to indicate something is a product.
     */
    val highlightProduct: Color,
    /**
     * Color to be used to indicate something is a substance.
     */
    val highlightSubstance: Color,
    /**
     * Color to be used to indicate something is a dose form or dose form characteristic.
     */
    val highlightDoseForm: Color,
    /**
     * Color to be used to indicate something is a dosage or amount.
     */
    val highlightDosage: Color,
    /**
     * Color to be used to indicate warnings.
     */
    val warning: Color,
) {
    val primary: Color get() = material.primary
    val primaryVariant: Color get() = material.primaryVariant
    val secondary: Color get() = material.secondary
    val secondaryVariant: Color get() = material.secondaryVariant
    val background: Color get() = material.background
    val surface: Color get() = material.surface
    val error: Color get() = material.error
    val onPrimary: Color get() = material.onPrimary
    val onSecondary: Color get() = material.onSecondary
    val onBackground: Color get() = material.onBackground
    val onSurface: Color get() = material.onSurface
    val onError: Color get() = material.onError
    val isLight: Boolean get() = material.isLight
}