package de.ywegel.svenska.domain.words

import de.ywegel.svenska.data.model.WordGroup

/**
 * Decides whether the forms of [FormBuilder] have to be checked by the user: the endings look wrong or the rules
 * can't build them safely.
 */
internal object FormReview {

    /**
     * @param stem The word the abbreviated endings are added to: svara, sök (söka).
     * @param forms The forms built from the endings.
     */
    fun needsReview(
        word: String,
        stem: String,
        endings: List<String>,
        forms: List<String>,
        wordGroup: WordGroup,
    ): Boolean = word in irregularWords ||
        forms.any { it.contains(tripleLetterRegex) } ||
        endings.any { it.isMalformed() || it.hasAbbreviatedVariants() } ||
        endings.any { it.isWronglyBuilt(stem) || it.doesNotFit(word, stem) } ||
        (wordGroup is WordGroup.Verb && endings.hasUnclearPresent(word)) ||
        (wordGroup is WordGroup.Noun && nounNeedsReview(word, endings))

    private fun nounNeedsReview(word: String, endings: List<String>): Boolean = endings.lacksEInDefinite(word) ||
        endings.hasUnclearE(word) ||
        endings.repeatsPluralEndingAfterUnchanged(word) ||
        hasUnexplainedPlural(word, endings)

    // -et/-en: only written out variants can be compared later
    private fun String.hasAbbreviatedVariants(): Boolean = isAbbreviated && variants().size > 1

    /**
     * Abbreviations the rules build wrong. Each occurs only a few times in the books, so they are flagged instead of
     * having a rule of their own.
     */
    private fun String.isWronglyBuilt(stem: String): Boolean = wronglyBuilt.any { (stemEnding, ending) ->
        this == ending && stem.contains(stemEnding)
    }

    /**
     * The ending can't be added to this word (bil + -an, hus + -t), or a shortened compound matches nowhere.
     */
    private fun String.doesNotFit(word: String, stem: String): Boolean = when {
        this == "-an" -> !stem.endsWith("a")
        !isAbbreviated || isUnchanged || isPureEnding(stem) -> false
        else -> overlayAbbreviated(word, withoutDash()) == null
    }

    // höra (-r, -de, -t) means hör, hörde, hört, but is written like svara (-r, -de, -t)
    private fun List<String>.hasUnclearPresent(word: String): Boolean =
        firstOrNull() == "-r" && word.contains(presentWithoutEndingRegex)

    // bil (-n), hus (-t): after a consonant only unstressed endings take a bare -n/-t: cykel -n, motor -n
    private fun List<String>.lacksEInDefinite(word: String): Boolean = firstOrNull() in bareDefiniteEndings &&
        !word.last().isVowel() &&
        unstressedNounEndings.none { word.endsWith(it) }

    /**
     * With -en/-et the e can stay (dataspel -> dataspelet) or drop (exempel -> exemplet). Only compounds of
     * stressed one syllable words are safe.
     */
    private fun List<String>.hasUnclearE(word: String): Boolean = firstOrNull() in fullDefiniteEndings &&
        word.hasEBeforeFinalLnr() &&
        word.isPolysyllabic() &&
        stressedNounEndings.none { word.endsWith(it) }

    // allergiker (-n, -, -erna): an unchanged plural only takes -na. Nouns on -are are built right
    private fun List<String>.repeatsPluralEndingAfterUnchanged(word: String): Boolean = size == NOUN_ENDINGS_COUNT &&
        this[1].isUnchanged &&
        this[2] in fullDefinitePluralEndings &&
        !word.endsWith("are")

    // klass (-en, -er, -arna) is a typo. man (mannen, män, männen) changes the vowel, so it is a real exception
    private fun hasUnexplainedPlural(word: String, endings: List<String>): Boolean {
        val (_, plural, definitePlural) = endings.takeIf { it.size == NOUN_ENDINGS_COUNT } ?: return false
        return hasInconsistentPlural(word, plural, definitePlural) && !hasVowelChangeInPlural(word, plural)
    }

    private val fullDefiniteEndings = setOf("-en", "-et")
    private val fullDefinitePluralEndings = setOf("-arna", "-erna", "-orna")
    private val unstressedNounEndings = setOf("el", "er", "or", "ar")
    private val stressedNounEndings = setOf("del", "spel", "fel", "ben", "sten", "sken")
    private val presentWithoutEndingRegex = Regex("[öäy]ra$")
    private val tripleLetterRegex = Regex("""(.)\1\1""")
    private val consonantAndTRegex = Regex("[^$VOWELS]t$")

    // Checked against the stem of verbs: gifta -> gift
    private val wronglyBuilt = listOf(
        Regex("(é|eri)$") to "-t", // kafé -> kaféet, skafferi -> skafferiet
        Regex("(eum|ium)$") to "-et", // museum -> museet
        consonantAndTRegex to "-te", // gifta -> gifte, smälta -> smälte
        consonantAndTRegex to "-t", // salt -> salt, gifta -> gift
        Regex("glad$") to "-t", // färgglad -> färgglatt, not färgglat like stressad -> stressat
    )

    // sen and ingen look like öppen, but are irregular: jättesent, jättesena; inget, inga
    private val irregularWords = setOf("ingen", "jättesen")

    private const val NOUN_ENDINGS_COUNT = 3
}
