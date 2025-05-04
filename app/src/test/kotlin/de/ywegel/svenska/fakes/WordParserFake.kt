package de.ywegel.svenska.fakes

import de.ywegel.svenska.data.WordParser
import de.ywegel.svenska.data.model.Vocabulary

class WordParserFake : WordParser {
    override fun parseWord(word: String?, translation: String?, containerId: Int): Vocabulary = Vocabulary(
        word = word.orEmpty(),
        translation = translation.orEmpty(),
        containerId = containerId,
    )
}
