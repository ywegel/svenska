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
        val forms = endings.map { buildForm(word, base, it) }
        return BuiltForms(
            forms = forms,
            needsReview = needsReview(word, base, endings, wordGroup) || forms.any { it.contains(tripleLetterRegex) },
        )
    }

    /**
     * @param text The word the abbreviated endings are added to: svara, sök (söka), spegel.
     * @param hasUnstressedE The e before the last l/n/r drops, when something is added: spegel -> speglar.
     * @param doublesLastLetter An ending starting with the last letter doubles it: program -met, get -ter, dum -ma.
     * Otherwise the book only repeats the letter the ending is attached to: blyg -gt, sköta -tte.
     */
    private data class Base(val text: String, val hasUnstressedE: Boolean, val doublesLastLetter: Boolean)

    /**
     * Verbs with "-r" in the present keep their -a (svara -> svarade), all others add their endings to the
     * stem (söka -> sökte). Nouns and adjectives always use the full word.
     *
     * The e is unstressed in polysyllabic adjectives on consonant + el/en/er (öppen, makaber) and in nouns of that
     * shape, whose definite singular is -n/-t or repeats the last letter (spegel -n, fönster -ret).
     * With -en/-et it is stressed and stays: fördel (-en, -ar) -> fördelar.
     *
     * Nouns double any last letter, verbs and adjectives only m and n.
     */
    private fun baseOf(word: String, endings: List<String>, wordGroup: WordGroup): Base {
        val keepsA = wordGroup !is WordGroup.Verb || endings.firstOrNull() == "-r"
        val text = if (keepsA) word else word.verbStem()
        return Base(
            text = text,
            hasUnstressedE = word.hasUnstressedE(endings.firstOrNull().orEmpty(), wordGroup),
            doublesLastLetter = wordGroup is WordGroup.Noun || text.last() in doubledLetters,
        )
    }

    private fun String.hasUnstressedE(definite: String, wordGroup: WordGroup): Boolean {
        if (!contains(unstressedERegex) || !isPolysyllabic()) return false
        return when (wordGroup) {
            WordGroup.Adjective -> true
            is WordGroup.Noun -> definite in bareDefiniteEndings || definite.withoutDash().startsWith(last())
            else -> false
        }
    }

    private fun buildForm(word: String, base: Base, ending: String): String = when {
        ending.isUnchanged -> word
        !ending.isAbbreviated -> overlay(word, ending) ?: ending
        ending.isPureEnding(base.text) -> append(base, ending.withoutDash())
        else -> overlayAbbreviated(word, ending.withoutDash()) ?: (word + ending.withoutDash())
    }

    private fun append(base: Base, ending: String): String {
        val text = base.text
        return when {
            // läkare + na -> läkarna, tränare + arna -> tränarna
            text.endsWith("are") && ending in definitePluralEndingsAfterAre -> text.dropLast(1) + "na"
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
            // blyg + gt -> blygt, sköt + tte -> skötte, vit + tt -> vitt
            !base.doublesLastLetter && ending.repeatsLastLetterOf(text) -> text.dropLast(1) + ending
            // penna + or -> pennor, flicka + an -> flickan
            text.endsWithDroppableVowel() && ending.startsWithVowel() -> join(text.dropLast(1), ending)
            // bil + ar -> bilar, program + met -> programmet
            else -> join(text, ending)
        }
    }

    // glömm + de -> glömde, tunn + t -> tunt. Before vowels the double consonant stays: glömmer, tunna
    private fun join(stem: String, ending: String): String =
        if (ending.startsWithVowel()) stem + ending else stem.withSingleFinalConsonant() + ending

    // -tte and -gt repeat the last letter, -te after möt is a plain ending: mötte
    private fun String.repeatsLastLetterOf(text: String): Boolean =
        startsWith(text.last()) && (this !in pureEndings || startsWith(text.takeLast(1).repeat(2)))

    /**
     * dd -> tt (nyfödd -> nyfött). Polysyllabic -ad -> -at (stressad -> stressat, friad -> friat).
     * Other vowel + d -> tt (glad -> glatt, betyd -> betytt). Consonant + d -> t (känd -> känt, använd -> använt).
     */
    private fun String.withDAssimilatedToT(): String = when {
        endsWith("dd") -> dropLast(2) + "tt"
        endsWith("ad") && isPolysyllabic() -> dropLast(1) + "t"
        dropLast(1).last().isVowel() -> dropLast(1) + "tt"
        else -> dropLast(1) + "t"
    }

    /**
     * -da and -dde always keep one d (skrämd -> skrämda, betyd -> betydde). -de only after consonant + d or
     * polysyllabic -ad (hänt -> hände, oväntad -> oväntade). After another vowel + d, the book means -dde:
     * föd + de -> födde.
     */
    private fun String.dropsDBefore(ending: String): Boolean = when (ending) {
        "da", "dde" -> true
        "de" -> !dropLast(1).last().isVowel() || (endsWith("ad") && isPolysyllabic())
        else -> false
    }

    // friad, stressad, but not glad
    private fun String.isPolysyllabic(): Boolean = vowels().length >= MIN_POLYSYLLABIC_VOWELS

    // spegel -> spegl, himmel -> himl
    private fun String.withoutUnstressedE(): String = dropLast(2).withSingleFinalConsonant() + last()

    // glömm -> glöm, tunn -> tun. Only mm and nn, other double consonants stay: fett, glatt
    private fun String.withSingleFinalConsonant(): String =
        if (takeLast(2) in shortenedDoubleConsonants) dropLast(1) else this

    /**
     * Lays a shortened compound over the end of the word, where the longest beginning of [piece] (at least
     * [MIN_OVERLAY_LENGTH] letters) appears: hurtbulle + bullar -> hurtbullar, trafikolycka + olyckor -> trafikolyckor.
     * Returns null if nothing matches.
     */
    private fun overlay(word: String, piece: String, minLength: Int = MIN_OVERLAY_LENGTH): String? {
        val start = (piece.length downTo minLength)
            .map { piece.take(it) }
            .firstOrNull { it in word }
            ?: return null
        return word.substringBeforeLast(start) + piece
    }

    /**
     * A vowel change leaves only two letters to match: storstad + städer -> storstäder, bidra + drog -> bidrog.
     * Short pieces would match by accident: kortfattad + at -> kortfat.
     */
    private fun overlayAbbreviated(word: String, piece: String): String? {
        val minLength = if (piece.length >= LONG_PIECE_LENGTH) MIN_LONG_PIECE_OVERLAY_LENGTH else MIN_OVERLAY_LENGTH
        return overlay(word, piece, minLength)
    }

    /**
     * A plain ending like -or, -arna, -dde, -e, optionally with the doubled last letter of the base:
     * program + -met, feber + -rar. Everything else is a shortened compound: -olyckor, -ministrar.
     */
    private fun String.isPureEnding(base: String): Boolean {
        val ending = withoutDash()
        return ending in pureEndings || (ending.startsWith(base.last()) && ending.drop(1) in pureEndings)
    }

    private fun needsReview(word: String, base: Base, endings: List<String>, wordGroup: WordGroup): Boolean =
        word in irregularWords ||
                endings.any { it.isMalformed() || it.hasAbbreviatedVariants() || it.isWronglyBuilt(base.text) } ||
                endings.any { it.doesNotFit(word, base.text) } ||
                (wordGroup is WordGroup.Verb && endings.hasUnclearPresent(word)) ||
                (wordGroup is WordGroup.Noun && nounNeedsReview(word, endings))

    private fun nounNeedsReview(word: String, endings: List<String>): Boolean = endings.lacksEInDefinite(word) ||
        endings.hasUnclearE(word) ||
        endings.repeatsPluralEndingAfterUnchanged(word) ||
        hasUnexplainedPlural(word, endings)

    // höra (-r, -de, -t) means hör, hörde, hört, but is written like svara (-r, -de, -t)
    private fun List<String>.hasUnclearPresent(word: String): Boolean =
        firstOrNull() == "-r" && word.contains(presentWithoutEndingRegex)

    /**
     * With -en/-et the e can stay (dataspel -> dataspelet) or drop (exempel -> exemplet). Only compounds of
     * stressed one syllable words are safe.
     */
    private fun List<String>.hasUnclearE(word: String): Boolean = firstOrNull() in fullDefiniteEndings &&
        word.contains(unstressedERegex) &&
        word.isPolysyllabic() &&
        stressedNounEndings.none { word.endsWith(it) }

    // allergiker (-n, -, -erna): an unchanged plural only takes -na. Nouns on -are are built right
    private fun List<String>.repeatsPluralEndingAfterUnchanged(word: String): Boolean =
        size == NOUN_ENDINGS_COUNT && this[1].isUnchanged && this[2] in fullDefinitePluralEndings && !word.endsWith("are")

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
    private fun String.doesNotFit(word: String, base: String): Boolean = when {
        this == "-an" -> !base.endsWith("a")
        !isAbbreviated || isUnchanged || isPureEnding(base) -> false
        else -> overlayAbbreviated(word, withoutDash()) == null
    }

    // bil (-n), hus (-t): after a consonant only unstressed endings take a bare -n/-t: cykel -n, motor -n
    private fun List<String>.lacksEInDefinite(word: String): Boolean =
        firstOrNull() in bareDefiniteEndings && !word.last().isVowel() && unstressedNounEndings.none { word.endsWith(it) }

    // klass (-en, -er, -arna) is a typo. man (mannen, män, männen) changes the vowel, so it is a real exception
    private fun hasUnexplainedPlural(word: String, endings: List<String>): Boolean {
        val (_, plural, definitePlural) = endings.takeIf { it.size == NOUN_ENDINGS_COUNT } ?: return false
        return hasInconsistentPlural(word, plural, definitePlural) && !hasVowelChangeInPlural(word, plural)
    }

    private val enToEtEndings = setOf("t", "et")
    private val bareDefiniteEndings = setOf("-n", "-t")
    private val shortenedDoubleConsonants = setOf("mm", "nn")
    private val doubledLetters = setOf('m', 'n')
    private val definitePluralEndingsAfterAre = setOf("na", "arna")
    private val fullDefiniteEndings = setOf("-en", "-et")
    private val fullDefinitePluralEndings = setOf("-arna", "-erna", "-orna")
    private val stressedNounEndings = setOf("del", "spel", "fel", "ben", "sten", "sken")
    private val presentWithoutEndingRegex = Regex("[öäy]ra$")
    private val tripleLetterRegex = Regex("""(.)\1\1""")
    private val unstressedNounEndings = setOf("el", "er", "or", "ar")
    private val unstressedERegex = Regex("[^aeiouyåäöé]e[lnr]$")
    private val pureEndings = setOf(
        "n", "en", "et", "t", "tt", "an", "a", "e", "r", "ar", "or", "er", "na", "ena", "rna", "arna", "orna", "erna",
        "de", "dde", "te", "da",
    )

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

    private const val MIN_OVERLAY_LENGTH = 3
    private const val MIN_LONG_PIECE_OVERLAY_LENGTH = 2
    private const val LONG_PIECE_LENGTH = 4
    private const val MIN_POLYSYLLABIC_VOWELS = 2
    private const val NOUN_ENDINGS_COUNT = 3
}
