package de.medizininformatikinitiative.medgraph.ui.navigation

import de.medizininformatikinitiative.medgraph.ui.UI

/**
 * Navigation component used to navigate around the application.
 *
 * @author Markus Budeus
 */
class Navigation {

    companion object {
        private var currentLocation: NavigationTarget = StubNavigationTarget()

        @JvmStatic
        fun navigateTo(target: NavigationTarget) {
            currentLocation.onNavigateAway()
            currentLocation = target
            target.onNavigateTo()
            UI.view = target.getUi()
        }
    }

}