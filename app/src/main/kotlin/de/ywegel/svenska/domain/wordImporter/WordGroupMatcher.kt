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
            else -> WordGroup.Other
        }
    }

    private fun isAdjective(endings: List<String>): Boolean {
        // Adjectives always only have 2 endings (mostly "-t", "-a")
        return endings.size == ADJECTIVE_ENDINGS_COUNT
    }

    private fun isNoun(endings: List<String>): Boolean {
        // Nouns always have 3 endings. Verbs also have 3, so this has to be checked after isVerb
        return endings.size == NOUN_ENDINGS_COUNT
    }

    private fun isVerb(endings: List<String>): Boolean {
        // Verbs always have 3 endings
        if (endings.size != VERB_ENDINGS_COUNT) return false

        // Verbs end with "ts" or 't' in the supine form
        val supineEnding = endings[2].trim()
        return verbIndicatorSuperlativEndings.any { supineEnding.endsWith(it, ignoreCase = true) }
    }

    private fun determineNounSubgroup(baseWord: String, endings: List<String>): WordGroup.Noun {
        val pluralEnding = endings[1]
        val lazyGender by lazy { determineGender(WordGroup.Noun(WordGroup.NounSubgroup.UNDEFINED), endings) }

        return when {
            // serie (-n -er -erna): serie + r, not seri + er
            baseWord.endsWith("e") && pluralEnding == "-er" -> WordGroup.Noun(WordGroup.NounSubgroup.R)
            // sko (-n skor skorna): sko + r, not sk + or
            baseWord.endsWith("o") && pluralEnding.endsWith("or") -> WordGroup.Noun(WordGroup.NounSubgroup.R)
            pluralEnding.endsWith("or") -> WordGroup.Noun(WordGroup.NounSubgroup.OR)
            pluralEnding.endsWith("ar") -> WordGroup.Noun(WordGroup.NounSubgroup.AR)
            pluralEnding.endsWith("er") -> WordGroup.Noun(WordGroup.NounSubgroup.ER)
            pluralEnding.endsWith("r") -> WordGroup.Noun(WordGroup.NounSubgroup.R)
            pluralEnding.endsWith("n") -> WordGroup.Noun(WordGroup.NounSubgroup.N)
            pluralEnding.endsWith("") && lazyGender == Gender.Neutra -> WordGroup.Noun(
                WordGroup.NounSubgroup.UNCHANGED_ETT,
            )

            pluralEnding.endsWith("") && lazyGender == Gender.Ultra -> WordGroup.Noun(
                WordGroup.NounSubgroup.UNCHANGED_EN,
            )

            else -> WordGroup.Noun(WordGroup.NounSubgroup.UNDEFINED)
        }
    }

    // TODO: if verb or noun has endings, but some endings do not have a '-', treat them as special!
    @Suppress("detekt:CyclomaticComplexMethod")
    private fun determineVerbSubgroup(baseWord: String, endings: List<String>): WordGroup.Verb {
        val present = endings[0]
        val past = endings[1]
        return when {
            // TODO: anpassen, dass ar auch -r matcht?
            baseWord.endsWith("a") && present.endsWith("ar") && past.endsWith("ade") -> WordGroup.Verb(
                WordGroup.VerbSubgroup.GROUP_1,
            )

            baseWord.endsWith("a") && present.endsWith("r") && past.endsWith("de") -> WordGroup.Verb(
                WordGroup.VerbSubgroup.GROUP_1,
            )

            baseWord.endsWith("a") && present.endsWith("er") && past.endsWith("de") -> WordGroup.Verb(
                WordGroup.VerbSubgroup.GROUP_2A,
            )

            baseWord.endsWith("a") && present.endsWith("er") && past.endsWith("te") -> WordGroup.Verb(
                WordGroup.VerbSubgroup.GROUP_2B,
            )

            baseWord.last() in setOf('a', 'e', 'i', 'o', 'u', 'y') &&
                present.endsWith("r") &&
                past.endsWith("dde")
            -> WordGroup.Verb(WordGroup.VerbSubgroup.GROUP_3)

            else -> WordGroup.Verb(WordGroup.VerbSubgroup.GROUP_4_SPECIAL)
        }
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
    const val NOUN_ENDINGS_COUNT = 3
    const val VERB_ENDINGS_COUNT = 3
    const val ADJECTIVE_ENDINGS_COUNT = 2
}
