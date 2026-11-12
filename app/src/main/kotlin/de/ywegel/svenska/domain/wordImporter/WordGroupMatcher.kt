package de.ywegel.svenska.domain.wordImporter

import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.data.model.WordGroup

object WordGroupMatcher {
    fun determineWordGroup(baseWord: String, endings: List<String>): WordGroup {
        // Check if is verb, bevor checking noun. Verbs are easiert to detect because of the "t"/"ts" ending in the Superlativ form
        return when {
            isAdjective(endings) -> WordGroup.Adjective
            isVerb(endings) -> determineVerbSubgroup(baseWord, endings)
            isNoun(endings) -> determineNounSubgroup(baseWord, endings)
            // Without a plural there is no subgroup to learn: mat (-en), kaffe (-t)
            isNounWithoutPlural(baseWord, endings) -> WordGroup.Noun(WordGroup.NounSubgroup.SPECIAL)
            isAdjectiveWithoutNeuter(baseWord, endings) -> WordGroup.Adjective
            else -> WordGroup.Other
        }
    }

    // Adjectives already ending on -t only list the plural: trött (-a), bäst (-a)
    private fun isAdjectiveWithoutNeuter(baseWord: String, endings: List<String>): Boolean {
        return singleEnding(baseWord, endings) == ADJECTIVE_PLURAL_ENDING
    }

    /**
     * Only the definite singular is given, which always ends on n or t: -en, -met, -ret, -vattnet.
     * Plural-only nouns (-na) and free text in parentheses (vard. för staden) stay out.
     */
    private fun isNounWithoutPlural(baseWord: String, endings: List<String>): Boolean {
        val ending = singleEnding(baseWord, endings) ?: return false
        return ending.startsWith("-") && ending.last() in definiteSingularLastLetters
    }

    // Written out forms are reduced to their ending: mat (maten) -> -en, trött (trötta) -> -a
    private fun singleEnding(baseWord: String, endings: List<String>): String? {
        val ending = endings.singleOrNull() ?: return null
        val isWrittenOut = ending.length > baseWord.length && ending.startsWith(baseWord, ignoreCase = true)
        return if (isWrittenOut) "-" + ending.substring(baseWord.length) else ending
    }

    private fun isAdjective(endings: List<String>): Boolean {
        // Adjectives always only have 2 endings (mostly "-t", "-a")
        return endings.size == ADJECTIVE_ENDINGS_COUNT
    }

    private fun isNoun(endings: List<String>): Boolean {
        // Nouns always have 3 endings. Verbs also have 3, so this has to be checked after isVerb
        return endings.size == NOUN_ENDINGS_COUNT
    }

    internal fun isVerb(endings: List<String>): Boolean {
        // Verbs always have 3 endings
        if (endings.size != VERB_ENDINGS_COUNT) return false

        // Verbs end with "ts" or 't' in the supine form
        val supineEnding = endings[2].trim()
        return verbIndicatorSuperlativEndings.any { supineEnding.endsWith(it, ignoreCase = true) }
    }

    private fun determineNounSubgroup(baseWord: String, endings: List<String>): WordGroup.Noun {
        val gender = determineGender(WordGroup.Noun(WordGroup.NounSubgroup.UNDEFINED), endings)
        if (isSpecialNoun(baseWord, endings, gender)) return WordGroup.Noun(WordGroup.NounSubgroup.SPECIAL)

        val pluralEnding = pluralEnding(baseWord, endings[1])

        return when {
            // serie (-n -er -erna): serie + r, not seri + er
            baseWord.endsWith("e") && pluralEnding == "-er" -> WordGroup.Noun(WordGroup.NounSubgroup.R)
            pluralEnding.endsWith("or") -> WordGroup.Noun(WordGroup.NounSubgroup.OR)
            pluralEnding.endsWith("ar") -> WordGroup.Noun(WordGroup.NounSubgroup.AR)
            pluralEnding.endsWith("er") -> WordGroup.Noun(WordGroup.NounSubgroup.ER)
            pluralEnding.endsWith("r") -> WordGroup.Noun(WordGroup.NounSubgroup.R)
            pluralEnding.endsWith("n") -> WordGroup.Noun(WordGroup.NounSubgroup.N)
            gender == Gender.Neutra -> WordGroup.Noun(WordGroup.NounSubgroup.UNCHANGED_ETT)
            // Nouns without a gender are already SPECIAL, so this is an en-word
            else -> WordGroup.Noun(WordGroup.NounSubgroup.UNCHANGED_EN)
        }
    }

    /**
     * Cuts the base word off written out plurals, so the group comes from what is added to the base word:
     * fiender -> r (fiende + r, not fiend + er), skor -> r (sko + r, not sk + or), bilar -> ar.
     * Abbreviated (-ar), shortened (bullar, -resor) and changed (sidor) plurals stay as they are.
     */
    private fun pluralEnding(baseWord: String, plural: String): String = when {
        // plåster (plåstret, plåster, plåstren) writes out the unchanged plural
        plural == baseWord -> "-"
        plural.startsWith(baseWord, ignoreCase = true) -> plural.substring(baseWord.length)
        else -> plural
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
        if (pluralStem.length < MIN_PLURAL_STEM_LENGTH) return false

        return pluralStem.vowels() !in baseWord.lowercase().vowels()
    }

    private fun String.vowels(): String = filter { it in VOWELS }

    private fun determineVerbSubgroup(baseWord: String, endings: List<String>): WordGroup.Verb {
        val present = endings[0]
        val past = endings[1].substringBefore("/")
        val supine = endings[2].substringBefore("/")

        val subgroup = when {
            isGroup1(baseWord, present, past, supine) -> WordGroup.VerbSubgroup.GROUP_1
            isGroup2(baseWord, past, supine) && past.endsWith("de") -> WordGroup.VerbSubgroup.GROUP_2A
            isGroup2(baseWord, past, supine) && past.endsWith("te") -> WordGroup.VerbSubgroup.GROUP_2B
            isGroup3(baseWord, present, past, supine) -> WordGroup.VerbSubgroup.GROUP_3
            else -> WordGroup.VerbSubgroup.GROUP_4_SPECIAL
        }

        return WordGroup.Verb(subgroup)
    }

    private fun isGroup1(baseWord: String, present: String, past: String, supine: String): Boolean {
        val isShortNotation = present == "-r" && past == "-de"
        val isWrittenOut = present.endsWith("ar") && past.endsWith("ade") && supine.endsWith("at")
        return baseWord.endsWith("a") && (isShortNotation || isWrittenOut)
    }

    private fun isGroup2(baseWord: String, past: String, supine: String): Boolean {
        val stem = baseWord.removeSuffix("a")
        return baseWord.endsWith("a") &&
            stem.length >= MIN_VERB_STEM_LENGTH &&
            past.keepsStem(stem) &&
            supine.keepsStem(stem) &&
            !supine.endsWith("it") &&
            !supine.endsWith("at")
    }

    // Double consonants can be simplified, so the last letter is ignored: glömma -> glömde, känna -> känt
    private fun String.keepsStem(stem: String): Boolean = startsWith("-") || startsWith(stem.dropLast(1))

    private fun isGroup3(baseWord: String, present: String, past: String, supine: String): Boolean {
        return baseWord.last() in group3WordEndings &&
            present.endsWith("r") &&
            past.endsWith("dde") &&
            supine.endsWith("tt")
    }

    fun determineGender(wordGroup: WordGroup, endings: List<String>): Gender? {
        if (endings.isEmpty()) return null
        if (wordGroup !is WordGroup.Noun) return null

        // The first form is the definite singular, for example: -n, -an, semestern, -t, -met, teet
        return when (endings[0].trim().lastOrNull()?.lowercaseChar()) {
            'n' -> Gender.Ultra // En-word
            't' -> Gender.Neutra // Ett-word
            else -> null // Unknown
        }
    }

    private val verbIndicatorSuperlativEndings = setOf("t", "ts")
    private val definiteSingularLastLetters = setOf('n', 't')
    private val group3WordEndings = setOf('e', 'i', 'o', 'u', 'y', 'å', 'ä', 'ö')
    private val pluralSuffixRegex = Regex("(or|ar|er|r|n)$")
    private val unchangedDefiniteRegex = Regex("(en|n|na)$")
    private val bareSuffixes = setOf("n", "en", "t", "et", "r", "ar", "or", "er", "na", "arna", "orna", "erna", "rna")
    private const val VOWELS = "aeiouyåäöé"
    private const val MIN_VERB_STEM_LENGTH = 2
    private const val MIN_PLURAL_STEM_LENGTH = 2
    private const val ADJECTIVE_PLURAL_ENDING = "-a"
    private const val NOUN_ENDINGS_COUNT = 3
    private const val VERB_ENDINGS_COUNT = 3
    private const val ADJECTIVE_ENDINGS_COUNT = 2
}
