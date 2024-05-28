package de.medizininformatikinitiative.medgraph.ui.graphdbpopulator

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen

/**
 * The UI screen which is used to populate the graph database.
 *
 * @author Markus Budeus
 */
class GraphDbPopulatorScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel = rememberScreenModel { GraphDbPopulatorScreenModel() }
        GraphDbPopulatorUI(viewModel, modifier = Modifier.padding(8.dp))
    }
}