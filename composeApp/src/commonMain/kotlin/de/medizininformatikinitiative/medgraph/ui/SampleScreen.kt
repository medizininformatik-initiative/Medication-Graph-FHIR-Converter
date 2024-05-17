package de.medizininformatikinitiative.medgraph.ui

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen

/**
 * @author Markus Budeus
 */
class SampleScreen : Screen {

    @Composable
    override fun Content() {
        Text("This is only a sample screen! But if you see this, it works!")
    }

}