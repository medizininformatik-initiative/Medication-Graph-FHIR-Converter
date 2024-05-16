package de.medizininformatikinitiative.medgraph.ui

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

/**
 * @author Markus Budeus
 */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "MedicationGraphFHIRConverter",
    ) {
        Application()
    }
}