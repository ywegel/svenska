package de.ywegel.svenska.data

import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.data.model.WordGroup

object WordGroupMatcher {
    fun extractWordAndEndings(word: String): Pair<String, List<String>> {
        val regex = Regex("""(.+?)\s*\(([^)]+)\)""")
        val matchResult = regex.find(word)
        return if (matchResult != null) {
            val baseWord = matchResult.groupValues[1]
            val endings = matchResult.groupValues[2].split(",").map { it.trim() }
            Pair(baseWord.trim(), endings)
        } else {
            Pair(word.trim(), emptyList())
        }
    }

    fun determineWordGroup(baseWord: String, endings: List<String>): WordGroup {
        return when {
            isAdjective(endings) -> WordGroup.Adjective
            isNoun(endings) -> determineNounSubgroup(endings)
            isVerb(endings) -> determineVerbSubgroup(baseWord, endings)
            else -> WordGroup.Other
        }
    }

    private fun isAdjective(endings: List<String>): Boolean {
        // Adjectives always only have 2 endings (mostly "-t", "-a")
        return endings.size == ADJECTIVE_ENDINGS
    }

    private fun isNoun(endings: List<String>): Boolean {
        // Nouns always have 3 endings and the first ending is -n/-en/-t/-et
        return endings.size == NOUN_ENDINGS && endings[0].removePrefix("-") in nounIndicatorEndings
    }

    private fun isVerb(endings: List<String>): Boolean {
        // Verbs always have 3 endings
        return endings.size == VERB_ENDINGS
    }

    private fun determineNounSubgroup(endings: List<String>): WordGroup.Noun {
        val pluralEnding = endings[1]
        val lazyGender by lazy { determineGender(WordGroup.Noun(WordGroup.NounSubgroup.UNDEFINED), endings) }

        return when {
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

    // TODO: if verb or noun has endings, but some endings to not have a '-', treat them as special!
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

        val firstEnding = endings[0].removePrefix("-")

        return when {
            firstEnding == "n" || firstEnding == "en" -> Gender.Ultra // En-word
            firstEnding == "t" || firstEnding == "et" -> Gender.Neutra // Ett-word
            else -> null // Unknown
        }
    }

    private val nounIndicatorEndings = setOf("n", "en", "t", "et")
    const val NOUN_ENDINGS = 3
    const val VERB_ENDINGS = 3
    const val ADJECTIVE_ENDINGS = 2
}
