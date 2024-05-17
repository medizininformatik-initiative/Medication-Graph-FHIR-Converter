package de.medizininformatikinitiative.medgraph.ui.common.db

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen

/**
 * [Screen]-implementation for the database connection dialog.
 *
 * @author Markus Budeus
 */
class ConnectionDialog : Screen {

    @Composable
    override fun Content() {
        val viewModel = rememberScreenModel { ConnectionDialogViewModel() }
        ConnectionDialogUI(viewModel, modifier = Modifier.padding(8.dp))
    }

}