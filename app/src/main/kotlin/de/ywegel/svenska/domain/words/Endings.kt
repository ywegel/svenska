package de.ywegel.svenska.domain.words

private const val VOWELS = "aeiouyåäöé"
private val droppableVowels = setOf('a', 'e')
private val bareSuffixes = setOf("n", "en", "t", "et", "r", "ar", "or", "er", "na", "arna", "orna", "erna", "rna")

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

fun String.startsWithVowel(): Boolean = firstOrNull()?.isVowel() == true

// penna + or -> pennor, same + er -> samer. Stressed vowels stay: bro + ar -> broar, kafé + er -> kaféer
fun String.endsWithDroppableVowel(): Boolean = lastOrNull()?.lowercaseChar() in droppableVowels

// block (et, -, -en) misses a dash and tillfälle (-t, -n, - na) has a space in the ending
fun String.isMalformed(): Boolean = this in bareSuffixes || contains(' ')
