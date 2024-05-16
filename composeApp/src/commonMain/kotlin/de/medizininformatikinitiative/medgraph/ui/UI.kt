package de.medizininformatikinitiative.medgraph.ui

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import de.medizininformatikinitiative.medgraph.ui.navigation.Drawable

/**
 * @author Markus Budeus
 */
class UI {

    companion object {
        var view by mutableStateOf<Drawable?>(null)

        @JvmStatic
        fun startUi() {
            application {
                Window(
                    onCloseRequest = ::exitApplication,
                    title = "MedicationGraphFHIRConverter",
                ) {
                    view?.Draw()
                }
            }
        }

    }
}