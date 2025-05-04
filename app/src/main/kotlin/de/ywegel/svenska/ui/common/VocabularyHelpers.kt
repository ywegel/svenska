package de.ywegel.svenska.ui.common

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

fun annotatedStringFromHighlights(
    word: String,
    highlights: List<Int>,
    style: SpanStyle = SpanStyle(fontWeight = FontWeight.Bold),
): AnnotatedString {
    return buildAnnotatedString {
        append(word)
        for (highlight in highlights.indices step 2) {
            addStyle(
                style = style,
                start = highlights[highlight],
                end = highlights[highlight + 1],
            )
        }
    }
}

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
