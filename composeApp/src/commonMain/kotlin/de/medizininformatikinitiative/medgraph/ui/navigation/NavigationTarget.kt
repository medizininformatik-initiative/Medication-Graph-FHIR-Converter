package de.medizininformatikinitiative.medgraph.ui.navigation

import androidx.compose.runtime.Composable

/**
 * Represents a part of the application you can navigate to.
 *
 * @author Markus Budeus
 */
interface NavigationTarget {

    /**
     * Called when the UI navigates to this target.
     */
    fun onNavigateTo()

    /**
     * Returns the UI drawer for this navigation target. This method may fail if not invoked between
     * [onNavigateTo] and [onNavigateAway].
     */
    fun getUi(): Drawable

    /**
     * Called when the user navigates away from this navigation target.
     */
    fun onNavigateAway()

}