package de.ywegel.svenska.domain.wordImporter

import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.domain.words.WordGroupMatcher
import javax.inject.Inject

interface WordParser {
    fun parseWord(word: String?, translation: String?, containerId: Int): Vocabulary
}

class WordParserImpl @Inject constructor() : WordParser {

    override fun parseWord(word: String?, translation: String?, containerId: Int): Vocabulary {
        val extractedWord = WordExtractor.extractWord(word.orEmpty())
        val endings = extractedWord.endings
        val wordGroup = WordGroupMatcher.determineWordGroup(extractedWord.baseWord, endings)

        // Other has no forms, so no changing word is split off and the bracket is kept, it holds no endings: i dag (adv.)
        if (wordGroup == WordGroup.Other) {
            return Vocabulary(
                word = with(WordExtractor) { word.orEmpty().normalizeSpecialCharacters().trim() },
                translation = translation.orEmpty(),
                wordGroup = wordGroup,
                containerId = containerId,
            )
        }

        return Vocabulary(
            word = extractedWord.baseWord,
            wordPrefix = extractedWord.prefix,
            wordSuffix = extractedWord.suffix,
            translation = translation.orEmpty(),
            wordHighlights = emptyList(),
            gender = WordGroupMatcher.determineGender(wordGroup, endings),
            wordGroup = wordGroup,
            ending = endings.joinToString(" "),
            containerId = containerId,
        )
    }
}
