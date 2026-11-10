package de.ywegel.svenska.domain.wordImporter

import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.WordGroup
import javax.inject.Inject

interface WordParser {
    fun parseWord(word: String?, translation: String?, containerId: Int): Vocabulary
}

class WordParserImpl @Inject constructor() : WordParser {

    override fun parseWord(word: String?, translation: String?, containerId: Int): Vocabulary {
        // TODO: Use prefix + suffix in Vocabulary, once the columns exist in the database
        val extractedWord = WordExtractor.extractWord(word.orEmpty().trim())
        val baseWord = listOf(extractedWord.prefix, extractedWord.baseWord)
            .filter { it.isNotEmpty() }
            .joinToString(" ")
        val endings = extractedWord.endings
        val wordGroup = WordGroupMatcher.determineWordGroup(baseWord, endings)
        val gender = WordGroupMatcher.determineGender(wordGroup, endings)

        val finalWordGroup = when {
            // Nouns without a plural are already SPECIAL and only have one ending
            wordGroup is WordGroup.Noun &&
                endings.size == WordGroupMatcher.NOUN_ENDINGS_COUNT &&
                isSpecialNoun(baseWord, endings, gender) ->
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

    private fun isSpecialNoun(baseWord: String, endings: List<String>, gender: Gender?): Boolean {
        val (_, plural, definitePlural) = endings
        return gender == null ||
            hasVowelChangeInPlural(baseWord, plural) ||
            endings.any { it.isMalformed() } ||
            hasInconsistentPlural(baseWord, plural, definitePlural)
    }

    // block (et, -, -en) misses a dash and tillfälle (-t, -n, - na) has a space in the ending
    private fun String.isMalformed(): Boolean = this in bareSuffixes || contains(' ')

    /**
     * Detects definite plurals that are not built from the indefinite plural.
     * Catches typos like klass (-en, -er, -arna), but also real irregular plurals like öga (ögat, ögon, ögonen),
     * where the regular pattern would be ögona.
     *
     * @param indefinitePluralEnding Second entry in the brackets. Only the ending ("-ar"), written out ("sidor"),
     * a shortened compound ("-resor") or "-" if unchanged.
     * @param definitePluralEnding Third entry in the brackets, written the same ways ("-arna", "sidorna").
     */
    private fun hasInconsistentPlural(
        baseWord: String,
        indefinitePluralEnding: String,
        definitePluralEnding: String,
    ): Boolean {
        val indefinitePlural = indefinitePluralEnding.removePrefix("-").lowercase()
        val definitePlural = definitePluralEnding.removePrefix("-").lowercase()

        // ord (-et, -, -en) and plåster (plåstret, plåster, plåstren). Catches fisk (-en, -, -ar)
        val isPluralUnchanged = indefinitePlural.isEmpty() || indefinitePlural == baseWord.lowercase()
        if (isPluralUnchanged) return !definitePlural.contains(unchangedDefiniteRegex)

        // bilar -> bilarna, yrken -> yrkena. Irregular plurals like "examen (-, examina, examina)" are skipped
        val definiteSuffix = when {
            indefinitePlural.endsWith("r") -> "na"
            indefinitePlural.endsWith("n") -> "a"
            else -> return false
        }

        // "arna" -> "ar"
        val definiteStem = definitePlural.removeSuffix(definiteSuffix)

        // Catches öga (ögon, ögonen), öra (öron, öronen) and man (män, männen): "en" instead of "a".
        // Also catches typos like klass (-en, -er, -arna) instead of -erna: "er" does not match "ar".
        // Both directions, as compounds are shortened on either side: -resor -> resorna, självkänslor -> -orna
        val definiteFollowsIndefinitePlural = definitePlural.endsWith(definiteSuffix) &&
            definiteStem.isNotEmpty() &&
            (indefinitePlural.endsWith(definiteStem) || definiteStem.endsWith(indefinitePlural))

        return !definiteFollowsIndefinitePlural
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
        val unchangedDefiniteRegex = Regex("(en|n|na)$")
        val bareSuffixes = setOf("n", "en", "t", "et", "r", "ar", "or", "er", "na", "arna", "orna", "erna", "rna")
        const val VOWELS = "aeiouyåäöé"
        const val MIN_STEM_LENGTH = 2
    }
}
