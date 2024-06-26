package de.medizininformatikinitiative.medgraph.ui

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import cafe.adriel.voyager.navigator.Navigator
import de.medizininformatikinitiative.medgraph.ui.common.db.ConnectionDialog
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
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
                    val windowState = rememberWindowState()
                    windowState.size = DpSize(1200.dp, 900.dp)

                    Window(
                        onCloseRequest = ::exitApplication,
                        state = windowState,
                        title = StringRes.title,
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