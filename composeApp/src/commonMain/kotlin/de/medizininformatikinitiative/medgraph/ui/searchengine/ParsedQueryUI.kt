package de.medizininformatikinitiative.medgraph.ui.searchengine

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.medizininformatikinitiative.medgraph.searchengine.model.Amount
import de.medizininformatikinitiative.medgraph.searchengine.model.Dosage
import de.medizininformatikinitiative.medgraph.searchengine.model.SearchQuery
import de.medizininformatikinitiative.medgraph.ui.theme.ApplicationTheme
import de.medizininformatikinitiative.medgraph.ui.theme.CorporateDesign
import de.medizininformatikinitiative.medgraph.ui.theme.TUMWeb
import java.math.BigDecimal

@Composable
@Preview
fun ParsedQueryUI() {
    ApplicationTheme {
        ParsedQueryUI(
            SearchQuery(
                "Aspirin HEXAL",
                "Acetylsalicylsäure Clopidogrel",
                listOf(Dosage.of(500, "mg"), Dosage.of(BigDecimal.TEN, "mg", BigDecimal.ONE, "ml")),
                listOf(Amount(BigDecimal.ONE, "ml"))
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ParsedQueryUI(query: SearchQuery, modifier: Modifier = Modifier) {

    Column(modifier = modifier) {

        val productName = query.productName
        val substanceName = query.substanceName

        if (productName != null) {
            Separator("Product name")
            TextBox(productName, textColor = CorporateDesign.Secondary.DarkBlue)
        }
        if (substanceName != null) {
            Separator("Substance name")
            TextBox(substanceName, textColor = CorporateDesign.Emphasis.Orange)
        }
        Separator("Dosages")
        Row {
            query.activeIngredientDosages.forEach { dosage ->
                TextBox(dosage.toString(), textColor = CorporateDesign.Emphasis.Green)
            }
        }
        Separator("Amounts")
        Row {
            query.drugAmounts.forEach { amount ->
                TextBox(amount.toString(), textColor = CorporateDesign.Emphasis.Green)
            }
        }

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
fun TextBox(text: String, textColor: Color = CorporateDesign.Secondary.DarkBlue, modifier: Modifier = Modifier) {
    Text(
        text,
        color = textColor,
        modifier = modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(TUMWeb.TumGrey7)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    )
}