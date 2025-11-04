package de.medizininformatikinitiative.medgraph.ui.desktop.fhirexporter

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen

/**
 * Screen class for the file FHIR exporter screen.
 *
 * @author Markus Budeus
 */
class FileFhirExporterScreen : Screen {

    @Composable
    override fun Content() {
        FileFhirExporterUI(rememberScreenModel { FileFhirExporterScreenModel() }, modifier = Modifier.padding(8.dp))
    }
}