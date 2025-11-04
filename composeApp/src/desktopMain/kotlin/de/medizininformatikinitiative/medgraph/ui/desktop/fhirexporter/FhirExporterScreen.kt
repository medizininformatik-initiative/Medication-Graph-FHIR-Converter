package de.medizininformatikinitiative.medgraph.ui.desktop.fhirexporter

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen

/**
 * Screen class for the FHIR exporter screen.
 *
 * @author Markus Budeus
 */
class FhirExporterScreen : Screen {

    @Composable
    override fun Content() {
        FhirExporterUI(rememberScreenModel { FileFhirExporterScreenModel() }, modifier = Modifier.padding(8.dp))
    }
}