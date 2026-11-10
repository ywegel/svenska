package de.ywegel.svenska.domain.wordImporter

data class ExtractedWord(
    val prefix: String,
    val baseWord: String,
    val endings: List<String>,
    val suffix: String,
)

object WordExtractor {
    private const val MAX_ENDINGS = 3

    // Brackets after a space or starting with a dash hold endings: tyska(-n). Other attached brackets belong to the
    // word: fing(er), mums(a), la(de). One level of nesting is allowed for endings like lägga (-er, la(de), lagt)
    private val endingsBracketRegex = Regex("""(?:(?<=\s)\(|\((?=-))((?:[^()]|\([^()]*\))*)\)""")

    // Explanations between the base word and the endings: säga till (någon) (-er, sa/sade, sagt)
    private val trailingExplanationsRegex = Regex("""(\s+\([^()]*\))+$""")

    /**
     * @param indices Position of the bracket in the input, from "(" to ")" inclusive.
     */
    private class EndingsBracket(val indices: IntRange, val endings: List<String>)

    /**
     * Splits "prefix baseWord (ending1, ending2, ending3) suffix" into its parts. Prefix and suffix can have any
     * number of words. The base word is the last word before the endings, except for verbs, where it is the first word.
     * Without an endings bracket, the whole input is the base word.
     */
    fun extractWord(word: String): ExtractedWord {
        val input = word.normalizePdfDashes().trim()
        val endingsBracket = findEndingsBracket(input)
            ?: return ExtractedWord(prefix = "", baseWord = input, endings = emptyList(), suffix = "")
        val endings = endingsBracket.endings

        val textBeforeEndings = input.substring(0, endingsBracket.indices.first).trim()
        val explanations = trailingExplanationsRegex.find(textBeforeEndings)?.value.orEmpty()
        val wordsBeforeEndings = textBeforeEndings.removeSuffix(explanations)
        val textAfterWords = explanations + input.substring(endingsBracket.indices.last + 1)

        // Verb phrases inflect the first word: flyga drake (-er, flög, flugit), gifta sig (-er, -te, -t)
        return if (WordGroupMatcher.isVerb(endings)) {
            ExtractedWord(
                prefix = "",
                baseWord = wordsBeforeEndings.substringBefore(" "),
                endings = endings,
                suffix = (wordsBeforeEndings.substringAfter(" ", missingDelimiterValue = "") + textAfterWords).trim(),
            )
        } else {
            ExtractedWord(
                prefix = wordsBeforeEndings.substringBeforeLast(" ", missingDelimiterValue = "").trim(),
                baseWord = wordsBeforeEndings.substringAfterLast(" "),
                endings = endings,
                suffix = textAfterWords.trim(),
            )
        }
    }

    /**
     * Brackets with a single ending are often explanations like (plural) or (ATP),
     * so the first bracket with several endings is preferred.
     */
    private fun findEndingsBracket(input: String): EndingsBracket? {
        val endingsBrackets = endingsBracketRegex.findAll(input)
            .mapNotNull { match ->
                val content = match.groupValues[1]
                val endings = content.split(",").map(String::trim)
                EndingsBracket(match.range, endings).takeIf { content.isNotBlank() && endings.size <= MAX_ENDINGS }
            }
            .toList()

        return endingsBrackets.firstOrNull { it.endings.size > 1 } ?: endingsBrackets.firstOrNull()
    }

    /**
     * Replace the long dashes that are wrongly extracted from the pdf
     */
    fun String.normalizePdfDashes(): String {
        return this
            .replace('\u2212', '-') // −
            .replace('\u2013', '-') // –
            .replace('\u2014', '-') // —
            .replace('\u2010', '-') // ‐
    }
}
