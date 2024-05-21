package de.medizininformatikinitiative.medgraph.ui.searchengine

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import de.medizininformatikinitiative.medgraph.searchengine.QueryExecutor
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.SimpleQueryExecutor
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.SimpleQueryParser

/**
 * Search Engine UI Screen.
 *
 * @author Markus Budeus
 */
class SearchEngineScreen : Screen {

    private val queryParser = SimpleQueryParser()
    private val queryExecutor: QueryExecutor

    init {

        queryExecutor = SimpleQueryExecutor()


    }

    @Composable
    override fun Content() {
        SearchEngineUI(
            rememberScreenModel { SearchEngineViewModel() },
            modifier = Modifier.padding(16.dp)
        )
    }


}