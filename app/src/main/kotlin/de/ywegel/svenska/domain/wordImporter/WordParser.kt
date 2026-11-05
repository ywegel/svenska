package de.ywegel.svenska.domain.wordImporter

import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.WordGroup
import javax.inject.Inject

interface WordParser {
    fun parseWord(word: String?, translation: String?, containerId: Int): Vocabulary
}

class WordParserImpl @Inject constructor() : WordParser {

    override fun parseWord(word: String?, translation: String?, containerId: Int): Vocabulary {
        val (baseWord, endings) = WordExtractor.extractWordAndEndings(word.orEmpty().trim())
        val wordGroup = WordGroupMatcher.determineWordGroup(baseWord, endings)
        val gender = WordGroupMatcher.determineGender(wordGroup, endings)

        val finalWordGroup = when {
            wordGroup is WordGroup.Noun && hasVowelChangeInPlural(baseWord, endings[1]) ->
                WordGroup.Noun(WordGroup.NounSubgroup.SPECIAL)

            else -> wordGroup
        }

        val newVocabulary = Vocabulary(
            word = baseWord,
            translation = translation.orEmpty(),
            wordHighlights = emptyList(),
            gender = gender,
            wordGroup = finalWordGroup,
            ending = endings.joinToString(" "),
            containerId = containerId,
        )

        return newVocabulary
    }

    /**
     * fot -> fötter changes the vowel, while sida -> sidor and semester -> semestrar only lose one.
     * Checks for a contained sequence, as compounds are often shortened: hurtbulle -> bullar.
     */
    private fun hasVowelChangeInPlural(baseWord: String, plural: String): Boolean {
        val pluralStem = plural.removePrefix("-").lowercase().replace(pluralSuffixRegex, "")
        if (pluralStem.length < MIN_STEM_LENGTH) return false

        return pluralStem.vowels() !in baseWord.lowercase().vowels()
    }

    private fun String.vowels(): String = filter { it in VOWELS }

    private companion object {
        val pluralSuffixRegex = Regex("(or|ar|er|r|n)$")
        const val VOWELS = "aeiouyåäöé"
        const val MIN_STEM_LENGTH = 2
    }
}
