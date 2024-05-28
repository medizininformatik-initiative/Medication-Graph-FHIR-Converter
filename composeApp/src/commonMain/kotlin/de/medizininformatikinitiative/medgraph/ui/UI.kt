package de.medizininformatikinitiative.medgraph.ui

import androidx.compose.ui.window.application
import de.medizininformatikinitiative.medgraph.ui.theme.ApplicationTheme
import de.medizininformatikinitiative.medgraph.ui.theme.templates.DirectoryChooser

/**
 * @author Markus Budeus
 */
class UI {

    companion object {

        @JvmStatic
        fun startUi(goToConnectionDialog: Boolean = false) {
            application {
                ApplicationTheme {
                    DirectoryChooser({})
//                    Window(
//                        onCloseRequest = ::exitApplication,
//                        title = "MedicationGraphFHIRConverter",
//                    ) {
//                        if (goToConnectionDialog) {
//                            Navigator(listOf(MainMenu(), ConnectionDialog()))
//                        } else {
//                            Navigator(MainMenu())
//                        }
//                    }
                }
            }
        }

    }
}