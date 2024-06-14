package de.medizininformatikinitiative.medgraph.ui.licenses

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

/**
 * Screen which displays open-source licenses.
 *
 * @author Markus Budeus
 */
class LicenseScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        LicensesUI(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            navigator.pop()
        }
    }
}