package de.medizininformatikinitiative.medgraph.ui.common.navigation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * Represents a part of the application you can navigate to.
 *
 * @author Markus Budeus
 */
class NavigationTarget {

    private var coroutineScope: CoroutineScope? = null

    fun onNavigateTo() {
        coroutineScope = CoroutineScope(SupervisorJob())
    }

    fun onNavigateAway() {
        coroutineScope?.cancel("User navigated away from owning navigation target.")
        coroutineScope = null;
    }


}