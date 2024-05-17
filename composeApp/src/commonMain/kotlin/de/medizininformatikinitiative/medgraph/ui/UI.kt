package de.medizininformatikinitiative.medgraph.ui

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import cafe.adriel.voyager.navigator.Navigator
import de.medizininformatikinitiative.medgraph.ui.common.db.ConnectionDialog

/**
 * @author Markus Budeus
 */
class UI {

    companion object {

        @JvmStatic
        fun startUi() {
            application {
                Window(
                    onCloseRequest = ::exitApplication,
                    title = "MedicationGraphFHIRConverter",
                ) {
                    Navigator(ConnectionDialog())
                }
            }
        }

    }
}