package de.medizininformatikinitiative.medgraph.ui.licenses

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.theme.ApplicationTheme
import de.medizininformatikinitiative.medgraph.ui.theme.templates.Button

@Composable
@Preview
internal fun LicensesUIPreview() {
    ApplicationTheme {
        LicensesUI(Modifier.fillMaxWidth().padding(8.dp), {})
    }
}

@Composable
fun LicensesUI(modifier: Modifier = Modifier, onReturn: () -> Unit) {
    Column(
        modifier
    ) {
        Button(onReturn) { Text(StringRes.do_return) }
        Text(StringRes.licenses_about)
        Spacer(modifier = Modifier.height(8.dp))

        Column(
            Modifier.verticalScroll(rememberScrollState())
        ) {
            for (s in LicenseProvider().licenses) {
                LicenseButton(s, modifier = Modifier.fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp)
                    .height(60.dp))
            }
        }
    }
}

@Composable
fun LicenseButton(license: License, modifier: Modifier) {
    var detailsVisible by remember { mutableStateOf(false) }

    if (detailsVisible) {
        LicenseDialog(license) { detailsVisible = false }
    }

    Button(
        onClick = {
            detailsVisible = true
        },
        modifier = modifier
    ) {
        Text(license.libraryName, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun LicenseDialog(license: License, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        val shape = RoundedCornerShape(8.dp)
        Box(
            Modifier.fillMaxWidth()
                .border(2.dp, color = MaterialTheme.colors.onSurface, shape)
                .background(MaterialTheme.colors.surface, shape)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(license.libraryName, style = MaterialTheme.typography.h4)
                val uriHandler = LocalUriHandler.current
                ClickableText(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = MaterialTheme.colors.primary)) {
                            append(license.url)
                        }
                    },
                    style = MaterialTheme.typography.subtitle1) {
                    uriHandler.openUri(license.url)
                }


                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(license.toString())
                }
            }
        }
    }
}