package de.ywegel.svenska.ui.addEdit

import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.domain.words.changingWordIndex
import de.ywegel.svenska.ui.addEdit.models.ViewWordGroup
import de.ywegel.svenska.ui.common.vocabulary.HighlightUtils

/**
 * The word field, split into the parts that are saved: "ett högsta dr*ö*m" -> article "ett", prefix "högsta",
 * word "dröm" with its highlights.
 *
 * @param words The words the user can choose the changing word from, without the article.
 * @param changingIndex Index of the changing word in [words]. Null for phrases under Other.
 * @param hasHighlightError Stars are unbalanced or outside the changing word, so the input can't be saved.
 */
data class WordInput(
    val article: String?,
    val words: List<String>,
    val changingIndex: Int?,
    val prefix: String,
    val word: String,
    val suffix: String,
    val wordHighlights: List<Pair<Int, Int>>,
    val hasHighlightError: Boolean,
) {
    val articleGender: Gender?
        get() = when (article) {
            "en" -> Gender.Ultra
            "ett" -> Gender.Neutra
            else -> null
        }

    companion object {
        private val wordRegex = Regex("""\S+""")

        /**
         * @param chosenIndex The changing word the user tapped. Without a valid choice, the same word as in the
         * importer is used: the first for verbs, the last for everything else.
         */
        fun parse(text: String, group: ViewWordGroup?, chosenIndex: Int?): WordInput {
            val (cleanText, highlights) = HighlightUtils.parseHighlights(text)
                .getOrElse { return parse(text.replace("*", ""), group, chosenIndex).copy(hasHighlightError = true) }

            if (group == ViewWordGroup.Other) return phrase(cleanText, highlights)

            val allWords = wordRegex.findAll(cleanText).toList()
            val article = allWords.firstOrNull()?.value?.lowercase()
                ?.takeIf { allWords.size > 1 && it.isArticleOf(group) }
            val words = if (article != null) allWords.drop(1) else allWords
            if (words.isEmpty()) return phrase(cleanText, highlights)

            val index = chosenIndex?.takeIf { it in words.indices }
                ?: changingWordIndex(words.size, isVerb = group == ViewWordGroup.Verb)
            val changingWord = words[index].range
            val highlightsInWord = highlights.filter { (start, end) ->
                start >= changingWord.first && end <= changingWord.last + 1
            }

            return WordInput(
                article = article,
                words = words.map { it.value },
                changingIndex = index,
                prefix = words.take(index).joinToString(" ") { it.value },
                word = words[index].value,
                suffix = words.drop(index + 1).joinToString(" ") { it.value },
                wordHighlights = highlightsInWord.map { (start, end) ->
                    start - changingWord.first to end - changingWord.first
                },
                hasHighlightError = highlightsInWord.size != highlights.size,
            )
        }

        private fun phrase(cleanText: String, highlights: List<Pair<Int, Int>>): WordInput {
            val leadingSpaces = cleanText.length - cleanText.trimStart().length
            return WordInput(
                article = null,
                words = emptyList(),
                changingIndex = null,
                prefix = "",
                word = cleanText.trim(),
                suffix = "",
                wordHighlights = highlights.map { (start, end) -> start - leadingSpaces to end - leadingSpaces },
                hasHighlightError = false,
            )
        }

        private fun String.isArticleOf(group: ViewWordGroup?): Boolean = when (group) {
            ViewWordGroup.Noun -> this == "en" || this == "ett"
            ViewWordGroup.Verb -> this == "att"
            else -> false
        }
    }
}
