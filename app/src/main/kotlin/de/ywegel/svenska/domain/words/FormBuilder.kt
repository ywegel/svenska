package de.ywegel.svenska.domain.words

import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.domain.words.FormBuilder.MIN_OVERLAY_LENGTH

/**
 * @param forms One form per ending, without prefix and suffix. Holds the best guess, even if [needsReview] is set.
 * @param needsReview The endings look wrong or can't be built safely, so the user has to check the forms.
 */
data class BuiltForms(val forms: List<String>, val needsReview: Boolean)

/**
 * Builds the written out forms from the base word and its endings:
 * penna (-n, -or, -orna) -> pennan, pennor, pennorna.
 */
object FormBuilder {

    fun build(word: String, endings: List<String>, wordGroup: WordGroup): BuiltForms {
        if (wordGroup == WordGroup.Other) return BuiltForms(forms = emptyList(), needsReview = false)

        val base = baseOf(word, endings, wordGroup)
        return BuiltForms(
            forms = endings.map { buildForm(word, base, it) },
            needsReview = needsReview(word, base, endings, wordGroup),
        )
    }

    /**
     * @param text The word the abbreviated endings are added to: svara, sök (söka), spegel.
     * @param hasUnstressedE The e before the last l/n/r drops, when something is added: spegel -> speglar.
     */
    private data class Base(val text: String, val hasUnstressedE: Boolean)

    /**
     * Verbs with "-r" in the present keep their -a (svara -> svarade), all others add their endings to the
     * stem (söka -> sökte). Nouns and adjectives always use the full word.
     *
     * The e is unstressed in polysyllabic adjectives on consonant + el/en/er (öppen, makaber) and in nouns of that
     * shape, whose definite singular is -n/-t or repeats the last letter (spegel -n, fönster -ret).
     * With -en/-et it is stressed and stays: fördel (-en, -ar) -> fördelar.
     */
    private fun baseOf(word: String, endings: List<String>, wordGroup: WordGroup): Base = TODO()

    private fun buildForm(word: String, base: Base, ending: String): String = when {
        ending.isUnchanged -> word
        !ending.isAbbreviated -> overlay(word, ending) ?: ending
        ending.isPureEnding(base.text) -> append(base, ending.withoutDash())
        else -> overlay(word, ending.withoutDash()) ?: (word + ending.withoutDash())
    }

    private fun append(base: Base, ending: String): String {
        val text = base.text
        return when {
            // glad -> glatt, känd -> känt, stressad -> stressat
            text.endsWith("d") && ending.startsWith("t") -> text.withDAssimilatedToT() + ending.trimStart('t')
            // skrämd + da -> skrämda, hända + de -> hände, betyda + dde -> betydde
            text.endsWith("d") && ending.startsWith("d") && text.dropsDBefore(ending) -> text.dropLast(1) + ending
            // öppen + t -> öppet, sugen + et -> suget
            base.hasUnstressedE && text.endsWith("en") && ending in enToEtEndings -> text.dropLast(2) + "et"
            // feber + rar -> febrar, fönster + ret -> fönstret, ljummen + na -> ljumna
            base.hasUnstressedE && ending.startsWith(text.last()) -> join(text.dropLast(2), ending)
            // spegel + ar -> speglar, öppen + a -> öppna, himmel + ar -> himlar
            base.hasUnstressedE && ending.startsWithVowel() -> join(text.withoutUnstressedE(), ending)
            // penna + or -> pennor, flicka + an -> flickan
            text.endsWithDroppableVowel() && ending.startsWithVowel() -> join(text.dropLast(1), ending)
            // bil + ar -> bilar, program + met -> programmet
            else -> join(text, ending)
        }
    }

    // glömm + de -> glömde, tunn + t -> tunt. Before vowels the double consonant stays: glömmer, tunna
    private fun join(stem: String, ending: String): String =
        if (ending.startsWithVowel()) stem + ending else stem.withSingleFinalConsonant() + ending

    /**
     * dd -> tt (nyfödd -> nyfött). Polysyllabic -ad -> -at (stressad -> stressat, friad -> friat).
     * Other vowel + d -> tt (glad -> glatt, betyd -> betytt). Consonant + d -> t (känd -> känt, använd -> använt).
     */
    private fun String.withDAssimilatedToT(): String = TODO()

    /**
     * -da and -dde always keep one d (skrämd -> skrämda, betyd -> betydde). -de only after consonant + d or
     * polysyllabic -ad (hänt -> hände, oväntad -> oväntade). After another vowel + d, the book means -dde:
     * föd + de -> födde.
     */
    private fun String.dropsDBefore(ending: String): Boolean = TODO()

    // spegel -> spegl, himmel -> himl
    private fun String.withoutUnstressedE(): String = TODO()

    // glömm -> glöm, tunn -> tun. Only mm and nn, other double consonants stay: fett, glatt
    private fun String.withSingleFinalConsonant(): String = TODO()

    /**
     * Lays a shortened compound over the end of the word, where the longest beginning of [piece] (at least
     * [MIN_OVERLAY_LENGTH] letters) appears: hurtbulle + bullar -> hurtbullar, trafikolycka + olyckor -> trafikolyckor.
     * Returns null if nothing matches.
     */
    private fun overlay(word: String, piece: String): String? = TODO()

    /**
     * A plain ending like -or, -arna, -dde, -e, optionally with the doubled last letter of the base:
     * program + -met, feber + -rar. Everything else is a shortened compound: -olyckor, -ministrar.
     */
    private fun String.isPureEnding(base: String): Boolean = TODO()

    private fun needsReview(word: String, base: Base, endings: List<String>, wordGroup: WordGroup): Boolean =
        word in irregularWords ||
                endings.any { it.isMalformed() || it.hasAbbreviatedVariants() || it.isWronglyBuilt(base.text) } ||
                endings.any { it.doesNotFit(word, base.text) } ||
                (wordGroup is WordGroup.Noun && hasUnexplainedPlural(word, endings))

    // -et/-en: only written out variants can be compared later
    private fun String.hasAbbreviatedVariants(): Boolean = isAbbreviated && variants().size > 1

    /**
     * Abbreviations the rules build wrong. Each occurs only a few times in the books, so they are flagged instead of
     * having a rule of their own.
     */
    private fun String.isWronglyBuilt(base: String): Boolean = wronglyBuilt.any { (baseEnding, ending) ->
        this == ending && base.contains(baseEnding)
    }

    /**
     * The ending can't be added to this word (bil + -an, hus + -t), or a shortened compound matches nowhere.
     */
    private fun String.doesNotFit(word: String, base: String): Boolean = TODO()

    // klass (-en, -er, -arna) is a typo. man (mannen, män, männen) changes the vowel, so it is a real exception
    private fun hasUnexplainedPlural(word: String, endings: List<String>): Boolean {
        val (_, plural, definitePlural) = endings.takeIf { it.size == NOUN_ENDINGS_COUNT } ?: return false
        return hasInconsistentPlural(word, plural, definitePlural) && !hasVowelChangeInPlural(word, plural)
    }

    private val enToEtEndings = setOf("t", "et")

    // Checked against the base, so verbs match with their stem: gifta -> gift
    private val wronglyBuilt = listOf(
        Regex("(é|eri)$") to "-t", // kafé -> kaféet, skafferi -> skafferiet
        Regex("(eum|ium)$") to "-et", // museum -> museet
        Regex("[^aeiouyåäöé]t$") to "-te", // gifta -> gifte, smälta -> smälte
        Regex("[^aeiouyåäöé]t$") to "-t", // salt -> salt, gifta -> gift
        Regex("glad$") to "-t", // färgglad -> färgglatt, not färgglat like stressad -> stressat
    )

    // sen and ingen look like öppen, but are irregular: jättesent, jättesena; inget, inga
    private val irregularWords = setOf("ingen", "jättesen")

    private const val MIN_OVERLAY_LENGTH = 2
    private const val NOUN_ENDINGS_COUNT = 3
}
