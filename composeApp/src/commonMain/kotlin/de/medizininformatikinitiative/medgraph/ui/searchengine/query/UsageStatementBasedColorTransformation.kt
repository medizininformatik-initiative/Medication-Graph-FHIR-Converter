package de.medizininformatikinitiative.medgraph.ui.searchengine.query

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import de.medizininformatikinitiative.medgraph.searchengine.tracing.SubstringUsageStatement

/**
 * A [VisualTransformation] which colors the text based on provided usage statements. Only works if the current text
 * matches the original value of the provided usage statements.
 *
 * @author Markus Budeus
 */
class UsageStatementBasedColorTransformation(vararg transformations: Any?) : VisualTransformation {

    val transformationMap: MutableMap<SubstringUsageStatement, Color> = HashMap()

    init {
        if (transformations.size % 2 != 0) throw IllegalArgumentException(
            "You must pass pairs of SubstringUsageStatement and Color as argument!"
        )
        for (i in 0..<transformations.size step 2) {
            if (transformations[i] != null) {
                transformationMap.put(transformations[i] as SubstringUsageStatement, transformations[i+1] as Color)
            }
        }
    }

    override fun filter(text: AnnotatedString): TransformedText {
        val builder = AnnotatedString.Builder(text)
        val rawText = text.text;
        transformationMap.forEach {
            if (it.key.original.equals(rawText)) {
                apply(it.key, it.value, builder)
            }
        }
        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }

    /**
     * Applies the given color all parts of the string used by the given usage statement.
     */
    private fun apply(usageStatement: SubstringUsageStatement, color: Color, to: AnnotatedString.Builder) {
        usageStatement.usedRanges.forEach {
            to.addStyle(SpanStyle(color), it.from, it.to)
        }
    }

}