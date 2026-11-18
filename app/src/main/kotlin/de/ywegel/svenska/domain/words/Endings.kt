@file:Suppress("TooManyFunctions")

package de.ywegel.svenska.domain.words

internal const val VOWELS = "aeiouyåäöé"
private const val MIN_POLYSYLLABIC_VOWELS = 2
private const val MIN_SHARED_STEM = 2
private val eBeforeFinalLnrRegex = Regex("[^$VOWELS]e[lnr]$")
private val droppableVowels = setOf('a', 'e')
// Definite singular without e, only after a vowel or an unstressed ending: penna -n, cykel -n, yrke -t
internal val bareDefiniteEndings = setOf("-n", "-t")
private val bareSuffixes = setOf("n", "en", "t", "et", "r", "ar", "or", "er", "na", "arna", "orna", "erna", "rna")
internal val pureEndings = setOf(
    "n", "en", "et", "t", "tt", "an", "a", "e", "r", "ar", "or", "er", "na", "ena", "rna", "arna", "orna", "erna",
    "de", "dde", "te", "da",
)

/** Endings can be separated by commas or spaces: "-er, -de, -t" and "-er -de -t". */
fun splitEndings(text: String): List<String> = text.split(',', ' ').map { it.trim() }.filter { it.isNotEmpty() }

/**
 * Splits a form into the part it shares with the base word and the part that changes: drömmen -> dröm + men.
 * Forms that barely share anything change completely: ta -> tog.
 */
fun String.splitAtChange(baseWord: String): Pair<String, String> {
    val shared = commonPrefixWith(baseWord).length
    return if (shared < MIN_SHARED_STEM) "" to this else take(shared) to drop(shared)
}

// "-or", "-n", "-" are abbreviated. "sidor", "pennan" are written out
val String.isAbbreviated: Boolean get() = startsWith("-")

// träd (-et, -, -en): the plural is the base word itself
val String.isUnchanged: Boolean get() = this == "-"

fun String.withoutDash(): String = removePrefix("-")

// sa/sade, glädjer/gläder
fun String.variants(): List<String> = split("/")

// söka -> sök, svara -> svar
fun String.verbStem(): String = removeSuffix("a")

fun Char.isVowel(): Boolean = lowercaseChar() in VOWELS

fun String.vowels(): String = filter { it.isVowel() }

// friad, stressad, but not glad
fun String.isPolysyllabic(): Boolean = vowels().length >= MIN_POLYSYLLABIC_VOWELS

// spegel, öppen, fönster: this e drops if it is unstressed. In fördel and dataspel it is stressed and stays
fun String.hasEBeforeFinalLnr(): Boolean = contains(eBeforeFinalLnrRegex)

fun String.startsWithVowel(): Boolean = firstOrNull()?.isVowel() == true

// penna + or -> pennor, same + er -> samer. Stressed vowels stay: bro + ar -> broar, kafé + er -> kaféer
fun String.endsWithDroppableVowel(): Boolean = lastOrNull()?.lowercaseChar() in droppableVowels

// block (et, -, -en) misses a dash and tillfälle (-t, -n, - na) has a space in the ending
fun String.isMalformed(): Boolean = this in bareSuffixes || contains(' ')

/**
 * A plain ending like -or, -arna, -dde, -e, optionally with the doubled last letter of the base:
 * program + -met, feber + -rar. Everything else is a shortened compound: -olyckor, -ministrar.
 *
 * @param base The word the ending is added to: program, sök (söka).
 */
fun String.isPureEnding(base: String): Boolean {
    val ending = withoutDash()
    return ending in pureEndings || (ending.startsWith(base.last()) && ending.drop(1) in pureEndings)
}
