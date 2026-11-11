package de.ywegel.svenska.domain.wordImporter

import de.ywegel.svenska.data.model.Vocabulary
import javax.inject.Inject

interface WordParser {
    fun parseWord(word: String?, translation: String?, containerId: Int): Vocabulary
}

class WordParserImpl @Inject constructor() : WordParser {

    override fun parseWord(word: String?, translation: String?, containerId: Int): Vocabulary {
        val extractedWord = WordExtractor.extractWord(word.orEmpty())
        val endings = extractedWord.endings
        val wordGroup = WordGroupMatcher.determineWordGroup(extractedWord.baseWord, endings)

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
