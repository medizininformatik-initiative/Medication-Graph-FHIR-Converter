package de.medizininformatikinitiative.medgraph.ui

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import cafe.adriel.voyager.navigator.Navigator
import de.medizininformatikinitiative.medgraph.ui.desktop.fhirexporter.FhirExporterScreen
import de.medizininformatikinitiative.medgraph.ui.desktop.graphdbpopulator.GraphDbPopulatorScreen
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.theme.templates.Button

@Composable
actual fun ColumnScope.MainMenuExtensions(
    navigator: Navigator,
    navigationButtonDesign: Modifier,
    buttonTextStyle: TextStyle
) {
    Button(
        { navigator.push(GraphDbPopulatorScreen()) },
        modifier = navigationButtonDesign
    ) {
        Text(StringRes.main_menu_populate_database, style = buttonTextStyle)
    }
    Button(
        { navigator.push(FhirExporterScreen()) },
        modifier = navigationButtonDesign
    ) {
        Text(StringRes.main_menu_fhir_exporter, style = buttonTextStyle)
    }
}