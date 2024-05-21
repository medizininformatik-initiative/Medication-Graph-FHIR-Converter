package de.medizininformatikinitiative.medgraph.ui

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import cafe.adriel.voyager.navigator.Navigator
import de.medizininformatikinitiative.medgraph.ui.common.db.ConnectionDialog
import de.medizininformatikinitiative.medgraph.ui.theme.ApplicationTheme

/**
 * @author Markus Budeus
 */
class UI {

    companion object {

        @JvmStatic
        fun startUi(goToConnectionDialog: Boolean = false) {
            application {
                ApplicationTheme {
                    Window(
                        onCloseRequest = ::exitApplication,
                        title = "MedicationGraphFHIRConverter",
                    ) {
                        if (goToConnectionDialog) {
                            Navigator(listOf(MainMenu(), ConnectionDialog()))
                        } else {
                            Navigator(MainMenu())
                        }
                    }
                }
            }
        }

    }
}