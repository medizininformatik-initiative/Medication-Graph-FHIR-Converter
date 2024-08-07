package de.medizininformatikinitiative.medgraph.ui.searchengine

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import de.medizininformatikinitiative.medgraph.DI
import de.medizininformatikinitiative.medgraph.common.db.DatabaseConnectionService
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.PerSessionQueryManager
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.WeightedScoringBasedQueryExecutor
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.*
import de.medizininformatikinitiative.medgraph.searchengine.db.Neo4jCypherDatabase
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.DetailedProduct
import de.medizininformatikinitiative.medgraph.searchengine.provider.Providers

/**
 * Search Engine UI Screen.
 *
 * @author Markus Budeus
 */
class SearchEngineScreen : Screen {

    private val queryManager: PerSessionQueryManager<DetailedProduct>

    init {
        queryManager =
            PerSessionQueryManager(
                { session ->
                    QueryRefinerImpl(
                        DosageQueryRefiner(),
                        DoseFormQueryRefiner(
                            Providers.getEdqmConceptIdentifiers(session)
                        ),
                        SubstanceQueryRefiner(
                            Providers.getSubstanceSynonyms(session)
                        )
                    )
                },
                ::WeightedScoringBasedQueryExecutor,
                DI.get(DatabaseConnectionService::class.java).createConnection()
            )
    }

    @Composable
    override fun Content() {
        SearchEngineUI(
            rememberScreenModel { SearchEngineViewModel(queryManager, queryManager) },
            modifier = Modifier.padding(16.dp)
        )
    }


}