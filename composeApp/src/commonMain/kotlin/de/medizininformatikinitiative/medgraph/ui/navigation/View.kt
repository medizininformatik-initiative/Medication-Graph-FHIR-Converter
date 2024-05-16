package de.medizininformatikinitiative.medgraph.ui.navigation

/**
 * Represents a UI view with a lifecycle.
 *
 * @author Markus Budeus
 */
interface View : Drawable {

    /**
     * Called when the view is no longer needed.
     */
    fun destroy()

}