package de.medizininformatikinitiative.medgraph.ui.navigation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * [View]-implementation which provides a coroutine scope during its lifetime.
 *
 * @author Markus Budeus
 */
abstract class CoroutineView(
    /**
     * The coroutine scope on this view.
     */
    protected val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob())
) : View {

    override fun destroy() {
        coroutineScope.cancel("User navigated away from owning view.")
    }

}