package de.medizininformatikinitiative.medgraph.ui.searchengine.pipeline.identifier

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Identifiable
import de.medizininformatikinitiative.medgraph.searchengine.model.identifiable.Product
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.Identifier
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.TrackableIdentifier
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.OriginalIdentifier
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.OriginalIdentifier.Source.*
import de.medizininformatikinitiative.medgraph.searchengine.model.identifier.TransformedIdentifier
import de.medizininformatikinitiative.medgraph.searchengine.provider.MappedIdentifier
import de.medizininformatikinitiative.medgraph.searchengine.stringtransformer.CollectionToLowerCase
import de.medizininformatikinitiative.medgraph.searchengine.stringtransformer.IdentityTransformer
import de.medizininformatikinitiative.medgraph.searchengine.stringtransformer.ListToSet
import de.medizininformatikinitiative.medgraph.searchengine.stringtransformer.MinimumTokenLength
import de.medizininformatikinitiative.medgraph.searchengine.stringtransformer.RemoveBlankStrings
import de.medizininformatikinitiative.medgraph.searchengine.stringtransformer.RemoveDosageInformation
import de.medizininformatikinitiative.medgraph.searchengine.stringtransformer.ToLowerCase
import de.medizininformatikinitiative.medgraph.searchengine.stringtransformer.Transformer
import de.medizininformatikinitiative.medgraph.searchengine.stringtransformer.TrimSpecialSuffixSymbols
import de.medizininformatikinitiative.medgraph.searchengine.stringtransformer.WhitespaceTokenizer
import de.medizininformatikinitiative.medgraph.ui.resources.StringRes
import de.medizininformatikinitiative.medgraph.ui.searchengine.results.DetailedIdentifiableObjectUI
import de.medizininformatikinitiative.medgraph.ui.theme.ApplicationTheme
import de.medizininformatikinitiative.medgraph.ui.theme.localColors
import de.medizininformatikinitiative.medgraph.ui.theme.templates.ContentCard
import de.medizininformatikinitiative.medgraph.ui.theme.templates.TextBox
import de.medizininformatikinitiative.medgraph.ui.theme.templates.TextBoxes

@Composable
@Preview
internal fun IdentifierUI() {
    var identifier: TrackableIdentifier<String> = OriginalIdentifier("Aspirin HEXAL", OriginalIdentifier.Source.KNOWN_IDENTIFIER)
    identifier = ToLowerCase().apply(identifier)
    val transformedIdentifier = WhitespaceTokenizer().apply(identifier)
    ApplicationTheme {
        IdentifierUI(
            identifier = transformedIdentifier,
            target = Product(14, "Aspirin HEXAL 100mg"),
            modifier = Modifier.padding(8.dp).fillMaxWidth()
        )
    }
}

/**
 * Displays the given identifier. If it's a mapped identifier, this includes displaying what it refers to. If it's
 * trackable, the origin sequence is shown.
 */
@Composable
@NonRestartableComposable
fun IdentifierUI(identifier: Identifier<*>, modifier: Modifier = Modifier) {
    when (identifier) {
        is MappedIdentifier<*, *> -> MappedIdentifierUI(identifier, modifier)
        is TrackableIdentifier<*> -> IdentifierUI(identifier, modifier)
        else -> GenericIdentifierUI(identifier, modifier)
    }
}

@Composable
@NonRestartableComposable
fun MappedIdentifierUI(identifier: MappedIdentifier<*, *>, modifier: Modifier = Modifier) =
    IdentifierUI(identifier.trackableIdentifier, modifier, identifier.target)

@Composable
fun IdentifierUI(identifier: TrackableIdentifier<out Any>, modifier: Modifier = Modifier, target: Identifiable? = null) {
    Column(modifier = modifier) {
        val localModifier = Modifier.fillMaxWidth()
        when (identifier) {
            is OriginalIdentifier<out Any> -> OriginalIdentifierUI(identifier, localModifier, target)
            is TransformedIdentifier<*, out Any> -> TransformedIdentifierUI(identifier, localModifier, target)
        }
    }
}

@Composable
fun OriginalIdentifierUI(identifier: OriginalIdentifier<out Any>, modifier: Modifier = Modifier, target: Identifiable? = null) {
    ContentCard(
        title = StringRes.original_identifier,
        description = getOriginalIdentifierSourceDescription(identifier.source),
        modifier = modifier
    ) {
        RawIdentifierUI(identifier.identifier)
        if (target != null) {
            Text("describes", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            DetailedIdentifiableObjectUI(target, backgroundColor = MaterialTheme.colors.background, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun TransformedIdentifierUI(identifier: TransformedIdentifier<*, out Any>, modifier: Modifier = Modifier, target: Identifiable? = null) {
    IdentifierUI(identifier.original, modifier, target)
    Spacer(modifier = Modifier.height(4.dp))
    ContentCard(
        description = describeTransformerAction(identifier.transformer),
        modifier = modifier
    ) {
        RawIdentifierUI(identifier.identifier)
    }
}

@Composable
fun GenericIdentifierUI(identifier: Identifier<*>, modifier: Modifier) {
    ContentCard(
        title = StringRes.generic_identifier,
        modifier = modifier
    ) {
        RawIdentifierUI(identifier.identifier)
    }
}

@Composable
fun RawIdentifierUI(rawIdentifier: Any) {
    val color = MaterialTheme.localColors.surface2
    when (rawIdentifier) {
        is Collection<*> -> TextBoxes(
            rawIdentifier,
            backgroundColor = color,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        )

        else -> TextBox(rawIdentifier.toString(), backgroundColor = color, modifier = Modifier.fillMaxWidth())
    }
}

private fun getOriginalIdentifierSourceDescription(source: OriginalIdentifier.Source): String {
    return when (source) {
        KNOWN_IDENTIFIER -> StringRes.identifier_source_known_identifier
        RAW_QUERY -> StringRes.identifier_source_raw_query
        SEARCH_QUERY -> StringRes.identifier_source_search_query
    }
}

internal fun describeTransformerAction(transformer: Transformer<*, *>): String {
    return when(transformer) {
        is CollectionToLowerCase -> StringRes.transformer_description_CollectionToLowerCase
        is IdentityTransformer -> StringRes.transformer_description_IdentityTransformer
        is ListToSet -> StringRes.transformer_description_ListToSet
        is MinimumTokenLength -> StringRes.get(StringRes.transformer_description_MinimumTokenLength, transformer.minLength)
        is RemoveBlankStrings -> StringRes.transformer_description_RemoveBlankStrings
        is RemoveDosageInformation -> StringRes.transformer_description_RemoveDosageInformation
        is ToLowerCase -> StringRes.transformer_description_ToLowerCase
        is TrimSpecialSuffixSymbols -> StringRes.transformer_description_TrimSpecialSuffixSymbols
        is WhitespaceTokenizer -> StringRes.transformer_description_WhitespaceTokenizer
        else -> StringRes.transformer_description_unknown
    }
}