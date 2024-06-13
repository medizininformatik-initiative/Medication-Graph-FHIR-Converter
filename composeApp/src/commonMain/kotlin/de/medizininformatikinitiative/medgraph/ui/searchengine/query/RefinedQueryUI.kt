package de.medizininformatikinitiative.medgraph.ui.searchengine.query

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.medizininformatikinitiative.medgraph.searchengine.algorithm.querymanagement.RefinedQuery
import de.medizininformatikinitiative.medgraph.searchengine.model.Amount
import de.medizininformatikinitiative.medgraph.searchengine.model.Dosage
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Matchable
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Substance
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.OriginalIdentifier
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.MatchingObject
import de.medizininformatikinitiative.medgraph.searchengine.model.matchingobject.OriginalMatch
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.searchengine.pipeline.PipelineInfoDialog
import de.medizininformatikinitiative.medgraph.ui.theme.ApplicationTheme
import de.medizininformatikinitiative.medgraph.ui.theme.localColors
import de.medizininformatikinitiative.medgraph.ui.theme.templates.TextBoxes
import java.math.BigDecimal

@Composable
@Preview
internal fun RefinedQueryUI() {
    ApplicationTheme {
        RefinedQueryUI(
            RefinedQuery.Builder()
                .withProductNameKeywords(
                    OriginalIdentifier(
                        listOf("Aspirin", "HEXAL"),
                        OriginalIdentifier.Source.RAW_QUERY
                    )
                )
                .withSubstance(OriginalMatch(Substance(1, "Acetylsalicylsäure")))
                .withSubstance(OriginalMatch(Substance(2, "Clopidogrel")))
                .withDosage(OriginalMatch(Dosage.of(500, "mg")))
                .withDosage(OriginalMatch(Dosage.of(BigDecimal.TEN, "mg", BigDecimal.ONE, "ml")))
                .withDrugAmount(OriginalMatch(Amount(BigDecimal.ONE, "ml")))
                .build(),
            modifier = Modifier.fillMaxWidth().padding(4.dp)
        )
    }
}

@Composable
fun RefinedQueryUI(query: RefinedQuery, modifier: Modifier = Modifier) {

    Column(
        modifier = modifier
            .heightIn(Dp.Unspecified, 300.dp)
            .verticalScroll(rememberScrollState())
    ) {
        var productKeywordsDetailsState by remember { mutableStateOf(false) }
        val moDetailsState = remember { mutableStateOf<MatchingObject<*>?>(null) }

        val moDetails = moDetailsState.value
        if (moDetails != null) {
            PipelineInfoDialog(moDetails, onDismissRequest = { moDetailsState.value = null })
        }
        if (productKeywordsDetailsState) {
            PipelineInfoDialog(query.productNameKeywords, onDismissRequest = { productKeywordsDetailsState = false })
        }

        Separator(StringRes.parsed_query_dialog_product_keywords)
        TextBoxes(query.productNameKeywords.identifier, textColor = MaterialTheme.localColors.highlightProduct,
            onClick = { productKeywordsDetailsState = true })
        Separator(StringRes.parsed_query_dialog_dosages)
        MoTextBoxes(query.dosages, textColor = MaterialTheme.localColors.highlightDosage,
            onClick = { obj -> moDetailsState.value = obj })
        Separator(StringRes.parsed_query_dialog_amounts)
        MoTextBoxes(query.drugAmounts, textColor = MaterialTheme.localColors.highlightDosage,
            onClick = { obj -> moDetailsState.value = obj })
        Separator(StringRes.parsed_query_dialog_dose_forms)
        MoTextBoxes(query.doseForms, textColor = MaterialTheme.localColors.highlightDoseForm,
            onClick = { obj -> moDetailsState.value = obj })
        Separator(StringRes.parsed_query_dialog_dose_form_characteristics)
        MoTextBoxes(query.doseFormCharacteristics, textColor = MaterialTheme.localColors.highlightDoseForm,
            onClick = { obj -> moDetailsState.value = obj })
        Separator(StringRes.parsed_query_dialog_substance)
        MoTextBoxes(query.substances, textColor = MaterialTheme.localColors.highlightSubstance,
            onClick = { obj -> moDetailsState.value = obj })

    }
}

@Composable
fun Separator(label: String, modifier: Modifier = Modifier.fillMaxWidth()) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(label, style = MaterialTheme.typography.caption)
        Divider(
            modifier
                .padding(8.dp)
                .weight(1f),
            thickness = 2.dp
        )
    }
}

@Composable
@NonRestartableComposable
private fun <T : Matchable> MoTextBoxes(
    objects: Iterable<MatchingObject<T>>,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colors.onSurface,
    onClick: ((MatchingObject<T>) -> Unit)? = null
) {
    TextBoxes(
        objects,
        modifier = modifier,
        textColor = textColor,
        displayNameExtractor = { mo -> mo.`object`.name },
        onClick = onClick
    )
}