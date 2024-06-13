package de.medizininformatikinitiative.medgraph.ui.searchengine.results

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.medizininformatikinitiative.medgraph.searchengine.model.Amount
import de.medizininformatikinitiative.medgraph.searchengine.model.CorrespondingActiveIngredient
import de.medizininformatikinitiative.medgraph.searchengine.model.Drug
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.*
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.theme.ApplicationTheme
import de.medizininformatikinitiative.medgraph.ui.theme.CorporateDesign
import de.medizininformatikinitiative.medgraph.ui.theme.localColors
import de.medizininformatikinitiative.medgraph.ui.theme.templates.TextBox
import de.medizininformatikinitiative.medgraph.ui.theme.templates.clipToBox
import java.math.BigDecimal

@Composable
@Preview
private fun IdentifiableObjectUI() {
    ApplicationTheme {
        Column {

            IdentifiableObjectUI(
                Product(
                    1,
                    "Furorese 100mg"
                ), modifier = Modifier.fillMaxWidth().padding(4.dp))
            Divider(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), thickness = 1.dp)
            DetailedIdentifiableObjectUI(
                DetailedProduct(
                    6,
                    "Prednisolut® 10 mg L, Pulver und Lösungsmittel zur Herstellung einer Injektionslösung",
                    listOf("01343446"),
                    listOf(
                        Drug(
                            "Pulver zur Herst. e. Inj.-Lsg.",
                            EdqmPharmaceuticalDoseForm(
                                "PDF-11205000",
                                "Powder for solution for injection",
                                emptyList()
                            ),
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
                modifier = Modifier.fillMaxWidth().padding(4.dp)
            )
        }
    }
}

/**
 * Basic matchable object information card which can be expanded into the detailed card.
 *
 * @param expanded whether this card is currently in the detailed mode
 * @param onSwitchExpand called when the card is being interacted with
 */
@Composable
fun ExpandableMatchableObjectUI(
    result: Matchable,
    expanded: Boolean,
    onSwitchExpand: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
    bottomSlot: @Composable ColumnScope.() -> Unit = {},
) {
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
            DetailedIdentifiableObjectUI(result, modifier = Modifier.fillMaxWidth(), bottomSlot = bottomSlot)
        } else {
            IdentifiableObjectUI(result, modifier = Modifier.fillMaxWidth())
        }
    }
}

/**
 * Displays a single identifiable object.
 */
@Composable
fun IdentifiableObjectUI(
    identifiable: Identifiable,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.surface,
) {
    TextBox(identifiable.name, backgroundColor = backgroundColor, modifier = modifier)
}

@Composable
fun DetailedIdentifiableObjectUI(
    identifiable: Identifiable,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.surface,
    bottomSlot: @Composable ColumnScope.() -> Unit = {},
) {
    when (identifiable) {
        is DetailedProduct -> DetailedProductResultUI(identifiable, modifier, backgroundColor, bottomSlot)
        is IdMatchable -> GenericIdMatchableObjectUI(identifiable, modifier, backgroundColor, bottomSlot)
        else -> IdentifiableObjectUI(identifiable, modifier, backgroundColor)
    }
}

@Composable
fun GenericDetailedMatchableObjectUI(
    matchable: Matchable,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.surface,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clipToBox(backgroundColor)
    ) {
        Text(matchable.name, style = MaterialTheme.typography.h6, color = CorporateDesign.Main.TUMBlue)
        Spacer(modifier = Modifier.height(4.dp))

        content()
    }
}

@Composable
fun GenericIdMatchableObjectUI(
    matchable: IdMatchable,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.surface,
    centerSlot: @Composable ColumnScope.() -> Unit = {},
    bottomRowSlot: @Composable RowScope.() -> Unit = {},
    bottomSlot: @Composable ColumnScope.() -> Unit = {},
) {
    GenericDetailedMatchableObjectUI(matchable, modifier, backgroundColor) {
        centerSlot()

        Spacer(modifier = Modifier.height(4.dp))
        Row {
            Text(StringRes.get(StringRes.result_mmi_id, matchable.id))
            bottomRowSlot()
        }
        bottomSlot()
    }
}

@Composable
fun DetailedProductResultUI(
    result: DetailedProduct, modifier: Modifier = Modifier, backgroundColor: Color,
    bottomSlot: @Composable ColumnScope.() -> Unit = {}
) {
    GenericIdMatchableObjectUI(
        result, modifier, backgroundColor,
        centerSlot = {
            result.drugs.forEach {
                DetailedDrugUI(it, modifier = Modifier.padding(2.dp).fillMaxWidth())
            }
        },
        bottomRowSlot = {
            val pzn = result.pzn.firstOrNull()
            if (pzn != null) {
                Spacer(modifier = Modifier.weight(1f))
                Text(StringRes.get(StringRes.result_pzn, pzn))
            }
        },
        bottomSlot = bottomSlot
    )
}

@Composable
fun DetailedDrugUI(drug: Drug, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clipToBox(MaterialTheme.localColors.surface2)
    ) {
        Text(drug.toString())

    }
}