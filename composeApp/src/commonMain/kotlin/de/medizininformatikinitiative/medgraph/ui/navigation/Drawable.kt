package de.medizininformatikinitiative.medgraph.ui.navigation

import androidx.compose.runtime.Composable

/**
 * A component capable of drawing UI.
 *
 * @author Markus Budeus
 */
@FunctionalInterface
interface Drawable {

    /**
     * Draws the UI of this view.
     */
    @Composable
    fun Draw()

}