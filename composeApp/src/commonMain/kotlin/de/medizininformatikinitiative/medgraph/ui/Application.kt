package de.medizininformatikinitiative.medgraph.ui

import androidx.compose.runtime.Composable
import de.medizininformatikinitiative.medgraph.ui.common.db.ConnectionDialog
import de.medizininformatikinitiative.medgraph.ui.common.db.ConnectionDialogViewModel
import kotlinx.coroutines.GlobalScope

/**
 * @author Markus Budeus
 */
@Composable
fun Application() {
    ConnectionDialog(ConnectionDialogViewModel(GlobalScope))
}