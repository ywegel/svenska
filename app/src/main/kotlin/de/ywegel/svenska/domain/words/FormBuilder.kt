package de.ywegel.svenska.domain.words

import de.ywegel.svenska.data.model.WordGroup

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
            needsReview = FormReview.needsReview(word, base.text, endings, forms, wordGroup),
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
            doublesLastLetter = wordGroup is WordGroup.Noun || text.takeLast(1).repeat(2) in doubledConsonants,
        )
    }

    private fun String.hasUnstressedE(definite: String, wordGroup: WordGroup): Boolean {
        if (!hasEBeforeFinalLnr() || !isPolysyllabic()) return false
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

    // spegel -> spegl, himmel -> himl
    private fun String.withoutUnstressedE(): String = dropLast(2).withSingleFinalConsonant() + last()

    // glömm -> glöm, tunn -> tun. Only mm and nn, other double consonants stay: fett, glatt
    private fun String.withSingleFinalConsonant(): String =
        if (takeLast(2) in doubledConsonants) dropLast(1) else this

    private val enToEtEndings = setOf("t", "et")
    private val doubledConsonants = setOf("mm", "nn")
    private val definitePluralEndingsAfterAre = setOf("na", "arna")
}
