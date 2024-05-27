package de.medizininformatikinitiative.medgraph.ui.searchengine

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.medizininformatikinitiative.medgraph.searchengine.model.Amount
import de.medizininformatikinitiative.medgraph.searchengine.model.Dosage
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.theme.ApplicationTheme
import de.medizininformatikinitiative.medgraph.ui.theme.CorporateDesign
import de.medizininformatikinitiative.medgraph.ui.theme.templates.clipToBox
import java.math.BigDecimal

@Composable
@Preview
fun ParsedQueryUI() {
    ApplicationTheme {
        ParsedQueryUI(
            SearchQuery(
                listOf("Aspirin", "HEXAL"),
                listOf("Acetylsalicylsäure", "Clopidogrel"),
                listOf(Dosage.of(500, "mg"), Dosage.of(BigDecimal.TEN, "mg", BigDecimal.ONE, "ml")),
                listOf(Amount(BigDecimal.ONE, "ml"))
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ParsedQueryUI(query: SearchQuery, modifier: Modifier = Modifier) {

    Column(
        modifier = modifier
            .heightIn(Dp.Unspecified, 300.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Separator(StringRes.parsed_query_dialog_product)
        TextBoxes(query.productNameKeywords, textColor = CorporateDesign.Secondary.DarkBlue)
        Separator(StringRes.parsed_query_dialog_substance)
        TextBoxes(query.substanceNameKeywords, textColor = CorporateDesign.Emphasis.Orange)
        Separator(StringRes.parsed_query_dialog_dosages)
        TextBoxes(query.activeIngredientDosages, textColor = CorporateDesign.Emphasis.Green)
        Separator(StringRes.parsed_query_dialog_amounts)
        TextBoxes(query.drugAmounts, textColor = CorporateDesign.Emphasis.Green)

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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TextBoxes(objects: Iterable<Any>, textColor: Color) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        objects.forEach { obj ->
            TextBox(obj.toString(), textColor = textColor)
        }
    }
}

@Composable
fun TextBox(
    text: String,
    textColor: Color = CorporateDesign.Secondary.DarkBlue,
    textAlign: TextAlign = TextAlign.Center,
    backgroundColor: Color = MaterialTheme.colors.surface,
    modifier: Modifier = Modifier
) {
    Text(
        text,
        color = textColor,
        textAlign = textAlign,
        modifier = modifier
            .clipToBox(backgroundColor)
    )
}