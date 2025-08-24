package de.ywegel.svenska.domain.wordImporter

import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.WordGroup

interface WordParser {
    fun parseWord(word: String?, translation: String?, containerId: Int): Vocabulary
}

class WordParserImpl : WordParser {

    override fun parseWord(word: String?, translation: String?, containerId: Int): Vocabulary {
        val (baseWord, endings) = WordGroupMatcher.extractWordAndEndings(word.orEmpty().trim())
        val wordGroup = WordGroupMatcher.determineWordGroup(baseWord, endings)
        val gender = WordGroupMatcher.determineGender(wordGroup, endings)

        // If we don't have any dashes in one ending, we can be sure that it is a special word
        val hasInvalidEndings = endings.any { !it.startsWith("-") }

        val finalWordGroup = when {
            hasInvalidEndings && wordGroup is WordGroup.Verb -> WordGroup.Verb(WordGroup.VerbSubgroup.GROUP_4_SPECIAL)
            hasInvalidEndings && wordGroup is WordGroup.Noun -> WordGroup.Noun(WordGroup.NounSubgroup.SPECIAL)
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
}
