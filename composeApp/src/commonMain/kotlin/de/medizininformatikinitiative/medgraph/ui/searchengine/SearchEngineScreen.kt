package de.medizininformatikinitiative.medgraph.ui.searchengine

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import de.medizininformatikinitiative.medgraph.common.db.ConnectionConfiguration
import de.medizininformatikinitiative.medgraph.searchengine.QueryExecutor
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.PerSessionQueryExecutor
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.SimpleQueryExecutor
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.initial.LevenshteinSearchMatchFinder
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.SimpleQueryParser
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.refining.ExperimentalRefiner
import de.medizininformatikinitiative.medgraph.searchengine.db.Neo4jCypherDatabase
import de.medizininformatikinitiative.medgraph.searchengine.provider.Providers

/**
 * Search Engine UI Screen.
 *
 * @author Markus Budeus
 */
class SearchEngineScreen : Screen {

    private val queryParser = SimpleQueryParser()
    private val queryExecutor: QueryExecutor;

    init {
        queryExecutor = PerSessionQueryExecutor(
            { session ->
                SimpleQueryExecutor(
                    LevenshteinSearchMatchFinder(
                        Providers.getProductSynonymes(session),
                        Providers.getSubstanceSynonymes(session)
                    ),
                    ExperimentalRefiner(session, Neo4jCypherDatabase(session))
                )
            },
            // TODO This may fail horribly, introduce better error management, i.e. display errors on UI
            ConnectionConfiguration.getDefault().createConnection()
        )
    }

    @Composable
    override fun Content() {
        SearchEngineUI(
            rememberScreenModel { SearchEngineViewModel(queryParser, queryExecutor) },
            modifier = Modifier.padding(16.dp)
        )
    }


}