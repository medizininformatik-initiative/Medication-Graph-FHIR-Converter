package de.medizininformatikinitiative.medgraph.ui.searchengine

import androidx.compose.animation.animateContentSize
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.medizininformatikinitiative.medgraph.searchengine.model.Amount
import de.medizininformatikinitiative.medgraph.searchengine.model.CorrespondingActiveIngredient
import de.medizininformatikinitiative.medgraph.searchengine.model.Drug
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.DetailedProduct
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Matchable
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.Product
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.theme.ApplicationTheme
import de.medizininformatikinitiative.medgraph.ui.theme.CorporateDesign
import de.medizininformatikinitiative.medgraph.ui.theme.TUMWeb
import de.medizininformatikinitiative.medgraph.ui.theme.templates.clipToBox
import java.math.BigDecimal


@Composable
@Preview
private fun SearchResultUI() {
    ApplicationTheme {
        Column {

            SearchResultUI(Product(1, "Furorese 100mg"), modifier = Modifier.fillMaxWidth())
            Divider(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), thickness = 1.dp)
            DetailedProductResultUI(
                DetailedProduct(
                    6,
                    "Prednisolut® 10 mg L, Pulver und Lösungsmittel zur Herstellung einer Injektionslösung",
                    listOf("01343446"),
                    listOf(
                        Drug(
                            "Pulver zur Herst. e. Inj.-Lsg.", "Powder for solution for injection",
                            Amount(BigDecimal.ONE, null),
                            listOf(
                                CorrespondingActiveIngredient(
                                    "Prednisolon 21-hydrogensuccinat, Natriumsalz",
                                    Amount(BigDecimal("10.48"), "mg"),
                                    "Prednisolon",
                                    Amount(BigDecimal("7.83"), "mg")
                                )
                            )
                        ),
                        Drug(
                            "Lösungsmittel", null,
                            Amount(BigDecimal(2), "ml"),
                            emptyList()
                        )
                    )
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ExpandableSearchResultUI(result: DetailedProduct,
                             expanded: Boolean,
                             onSwitchExpand: (Boolean) -> Unit = {},
                             modifier: Modifier = Modifier) {
    Box(modifier = modifier
        .animateContentSize()
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = LocalIndication.current
        ) {
            onSwitchExpand(!expanded)
        }
    ) {
        if (expanded) {
            DetailedProductResultUI(result, modifier = Modifier.fillMaxWidth())
        } else {
            SearchResultUI(result, modifier = Modifier.fillMaxWidth())
        }
    }
}

/**
 * Displays a single search result.
 */
@Composable
fun SearchResultUI(result: Matchable, modifier: Modifier = Modifier) {
    TextBox(result.name, modifier = modifier)
}

@Composable
fun DetailedProductResultUI(result: DetailedProduct, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(4.dp)
            .clipToBox()
    ) {
        Text(result.name, style = MaterialTheme.typography.h6, color = CorporateDesign.Secondary.DarkBlue)
        Spacer(modifier = Modifier.height(4.dp))

        result.drugs.forEach {
            DetailedDrugUI(it, modifier = Modifier.padding(2.dp).fillMaxWidth())
        }

        Spacer(modifier = Modifier.height(4.dp))
        Row {
            Text(StringRes.get(StringRes.result_mmi_id, result.id))
            val pzn = result.pzn.firstOrNull()
            if (pzn != null) {
                Spacer(modifier = Modifier.weight(1f))
                Text(StringRes.get(StringRes.result_pzn, pzn))
            }
        }
    }
}

@Composable
fun DetailedDrugUI(drug: Drug, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clipToBox(TUMWeb.TumBlueBright1)
    ) {
        Text(drug.toString())

    }
}