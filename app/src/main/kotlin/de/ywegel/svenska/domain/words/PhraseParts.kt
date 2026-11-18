package de.ywegel.svenska.domain.words

data class PhraseParts(val prefix: String, val word: String, val suffix: String)

private val wordRegex = Regex("""\S+""")

/** Verb phrases inflect the first word: flyga drake (-er, flög, flugit), gifta sig (-er, -te, -t). Others the last. */
fun changingWordIndex(wordCount: Int, isVerb: Boolean): Int = if (isVerb) 0 else wordCount - 1

/** Splits [phrase] around the word at [changingWordIndex]. */
fun splitPhrase(phrase: String, isVerb: Boolean): PhraseParts {
    val words = wordRegex.findAll(phrase).toList()
    if (words.isEmpty()) return PhraseParts(prefix = "", word = "", suffix = "")

    val changingWord = words[changingWordIndex(words.size, isVerb)]
    return PhraseParts(
        prefix = phrase.substring(0, changingWord.range.first).trim(),
        word = changingWord.value,
        suffix = phrase.substring(changingWord.range.last + 1).trim(),
    )
}
