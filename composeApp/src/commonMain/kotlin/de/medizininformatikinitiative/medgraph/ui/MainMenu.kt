package de.medizininformatikinitiative.medgraph.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import de.medizininformatikinitiative.medgraph.ui.common.db.ConnectionDialog
import de.medizininformatikinitiative.medgraph.ui.licenses.LicenseDialog
import de.medizininformatikinitiative.medgraph.ui.licenses.LicenseScreen
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.searchengine.SearchEngineScreen
import de.medizininformatikinitiative.medgraph.ui.theme.templates.Button

/**
 * Main Menu Screen.
 *
 * @author Markus Budeus
 */
class MainMenu : Screen {

    @Composable
    override fun Content() {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {

            val navigator = LocalNavigator.currentOrThrow
            val buttonModifier = Modifier.fillMaxWidth().height(80.dp)

            MainMenuExtensions(navigator, buttonModifier)

            Button(
                { navigator.push(SearchEngineScreen()) },
                modifier = buttonModifier
            ) {
                Text(StringRes.main_menu_search_algorithm)
            }

            Button(
                { navigator.push(ConnectionDialog()) },
                modifier = buttonModifier
            ) {
                Text(StringRes.main_menu_configure_db)
            }
            Button(
                { navigator.push(LicenseScreen()) },
                modifier = buttonModifier
            ) {
                Text(StringRes.main_menu_show_licenses)
            }

            Button(
                { System.exit(0) },
                modifier = buttonModifier
            ) {
                Text(StringRes.exit)
            }

        }
    }

}

@Composable
expect fun ColumnScope.MainMenuExtensions(navigator: Navigator, navigationButtonDesign: Modifier)