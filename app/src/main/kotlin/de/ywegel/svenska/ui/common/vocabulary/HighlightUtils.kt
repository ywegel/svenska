package de.ywegel.svenska.ui.common.vocabulary

import android.util.Log
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

object HighlightUtils {

    private const val TAG = "HighlightUtils"

    /**
     * Parses a string like "fr*å*ga" or "t*abc*est" into a clean word and a list of highlight ranges.
     * Each range represents the start and end indices of characters between '*' pairs to be highlighted.
     * Returns a Result with the clean word and highlight ranges, or fails with [UnbalancedHighlightsError] if '*' count is odd.
     */
    fun parseHighlights(input: String): Result<Pair<String, List<Pair<Int, Int>>>> {
        var starCount = 0
        val highlightRanges = mutableListOf<Pair<Int, Int>>()
        val cleanWord = StringBuilder(input.length)
        var startIndex: Int? = null

        input.forEachIndexed { index, char ->
            if (char == '*') {
                starCount++
                if (starCount % 2 == 1) { // Start of a highlight
                    startIndex = cleanWord.length
                } else { // End of a highlight
                    startIndex?.let { start ->
                        highlightRanges.add(start to cleanWord.length)
                    }
                    startIndex = null
                }
            } else {
                cleanWord.append(char)
            }
        }

        if (starCount % 2 != 0 || startIndex != null) {
            return Result.failure(UnbalancedHighlightsError(starCount))
        }

        return Result.success(cleanWord.toString() to highlightRanges)
    }

    /**
     * Reconstructs the original string with '*' from clean word and highlight ranges.
     * Inserts '*' at each range's start and end positions.
     * Skips invalid ranges (negative indices, start > end, or end > word length) with a debug log.
     */
    fun reconstructWithStars(word: String, highlightRanges: List<Pair<Int, Int>>): String {
        val result = StringBuilder(word)
        highlightRanges.reversed().forEach { (start, end) ->
            if (start < 0 || end < start || end > word.length) {
                Log.w(
                    TAG,
                    "Skipping invalid highlight range: start=$start, end=$end for word='$word' (length=${word.length})",
                )
                return@forEach // Skip invalid range
            }
            result.insert(end, '*')
            result.insert(start, '*')
        }
        return result.toString()
    }

    // TODO: Snapshot tests, as soon as the paparazzi is available
    /**
     * Builds an AnnotatedString with bold spans for the specified highlight ranges.
     * Applies bold style to characters between each range's start and end indices.
     */
    fun buildAnnotatedWord(word: String, highlightRanges: List<Pair<Int, Int>>): AnnotatedString {
        return buildAnnotatedString {
            append(word)
            highlightRanges.forEach { (start, end) ->
                addStyle(
                    style = SpanStyle(fontWeight = FontWeight.Bold),
                    start = start,
                    end = end,
                )
            }
        }
    }
}

fun String.parseHighlights(): Result<Pair<String, List<Pair<Int, Int>>>> = HighlightUtils.parseHighlights(this)
fun Pair<String, List<Pair<Int, Int>>>.reconstructWithStars(): String =
    HighlightUtils.reconstructWithStars(first, second)

fun Pair<String, List<Pair<Int, Int>>>.toAnnotatedWord(): AnnotatedString =
    HighlightUtils.buildAnnotatedWord(first, second)

/**
 * Thrown when the input string has an odd number of '*' delimiters, indicating unbalanced highlights.
 */
class UnbalancedHighlightsError(starCount: Int) :
    Throwable("Unbalanced amount of highlights found. It should be even, but was $starCount")
