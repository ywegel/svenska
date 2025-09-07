package de.ywegel.svenska.ui.common.vocabulary

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

// TODO: Refactor the number screen. Don't extract highlights from *, but rather supply the correct highlight locations
fun stringWithHighlightSeparators(
    input: String,
    style: SpanStyle = SpanStyle(fontWeight = FontWeight.Bold),
): AnnotatedString {
    return buildAnnotatedString {
        var markStart: Int? = null
        var marksFound = 0

        // TODO: Does not work if more than 2 * !!
        input.forEachIndexed { index, c ->
            if (c == '*') {
                if (markStart == null) {
                    markStart = index - marksFound
                } else {
                    addStyle(
                        style,
                        markStart!!,
                        index - marksFound,
                    )
                }
                marksFound += 1
            } else {
                append(c)
            }
        }
    }
}
