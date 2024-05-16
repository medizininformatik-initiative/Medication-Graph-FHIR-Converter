package de.medizininformatikinitiative.medgraph.ui.navigation

/**
 * A view which can be passed a callback to invoke when it finishes.
 *
 * @author Markus Budeus
 */
abstract class FinishableView(
    private val onFinish: () -> Unit,
) : View {

    protected fun finish() = onFinish()

}