package de.medizininformatikinitiative.medgraph.ui.licenses

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.theme.ApplicationTheme
import de.medizininformatikinitiative.medgraph.ui.theme.localColors
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

        val lp = LicenseProvider()

        Text(StringRes.license_about)
        Spacer(modifier = Modifier.height(8.dp))
        LicenseButton(
            lp.license,
            modifier = Modifier.fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .height(60.dp),
            buttonColors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.localColors.primaryVariant)
        )

        Text(StringRes.dependency_licenses_about)
        Spacer(modifier = Modifier.height(8.dp))

        Column(
            Modifier.verticalScroll(rememberScrollState())
        ) {
            for (s in lp.dependencyLicenses) {
                LicenseButton(
                    s, modifier = Modifier.fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                        .height(60.dp)
                )
            }
        }
    }
}

@Composable
fun LicenseButton(license: License, modifier: Modifier, buttonColors: ButtonColors = ButtonDefaults.buttonColors()) {
    var detailsVisible by remember { mutableStateOf(false) }

    if (detailsVisible) {
        LicenseDialog(license) { detailsVisible = false }
    }

    Button(
        onClick = {
            detailsVisible = true
        },
        modifier = modifier,
        colors = buttonColors,
    ) {
        Text(license.libraryName, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun LicenseDialog(license: License, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss,
        DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val shape = RoundedCornerShape(8.dp)
        Box(
            Modifier.width(800.dp)
                .border(2.dp, color = MaterialTheme.colors.onSurface, shape)
                .background(MaterialTheme.colors.surface, shape)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(license.libraryName, style = MaterialTheme.typography.h4)
                val uriHandler = LocalUriHandler.current
                ClickableText(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = MaterialTheme.colors.primary)) {
                            append(license.url)
                        }
                    },
                    style = MaterialTheme.typography.subtitle1
                ) {
                    uriHandler.openUri(license.url)
                }


                Spacer(modifier = Modifier.height(16.dp))

                var content by remember { mutableStateOf(license.toString()) }

                if (license.notice != null) {
                    NoticeOrLicenseTextSelectionButtons(
                        onSelectLicense = { content = license.toString() },
                        onSelectNotice = { content = license.notice },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(content)
                }
            }
        }
    }
}

@Composable
fun NoticeOrLicenseTextSelectionButtons(
    onSelectLicense: () -> Unit,
    onSelectNotice: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onSelectLicense, modifier = Modifier.weight(1f)) { Text(StringRes.licenses_license_text) }
        Button(onSelectNotice, modifier = Modifier.weight(1f)) { Text(StringRes.licenses_notice_text) }
    }
}
