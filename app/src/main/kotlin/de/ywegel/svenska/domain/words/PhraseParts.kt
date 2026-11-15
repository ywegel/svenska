package de.ywegel.svenska.domain.words

data class PhraseParts(val prefix: String, val word: String, val suffix: String)

// Verb phrases inflect the first word: flyga drake (-er, flög, flugit), gifta sig (-er, -te, -t)
fun splitPhrase(phrase: String, isVerb: Boolean): PhraseParts = if (isVerb) {
    PhraseParts(
        prefix = "",
        word = phrase.substringBefore(" "),
        suffix = phrase.substringAfter(" ", missingDelimiterValue = "").trim(),
    )
} else {
    PhraseParts(
        prefix = phrase.substringBeforeLast(" ", missingDelimiterValue = "").trim(),
        word = phrase.substringAfterLast(" "),
        suffix = "",
    )
}
