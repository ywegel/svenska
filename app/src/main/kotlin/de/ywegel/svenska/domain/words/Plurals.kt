package de.ywegel.svenska.domain.words

private val pluralSuffixRegex = Regex("(or|ar|er|r|n)$")
private val unchangedDefiniteRegex = Regex("(en|n|na)$")
private const val MIN_PLURAL_STEM_LENGTH = 2

/**
 * Detects definite plurals that are not built from the indefinite plural.
 * Catches typos like klass (-en, -er, -arna), instead of -erna, but also real irregular plurals like öga (ögat, ögon, ögonen),
 * where the regular pattern would be ögona.
 *
 * @param indefinitePluralEnding Second entry in the brackets. Only the ending ("-ar"), written out ("sidor"),
 * a shortened compound ("-resor") or "-" if unchanged.
 * @param definitePluralEnding Third entry in the brackets, written the same ways ("-arna", "sidorna").
 */
fun hasInconsistentPlural(baseWord: String, indefinitePluralEnding: String, definitePluralEnding: String): Boolean {
    val indefinitePlural = indefinitePluralEnding.withoutDash().lowercase()
    val definitePlural = definitePluralEnding.withoutDash().lowercase()

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
fun hasVowelChangeInPlural(baseWord: String, plural: String): Boolean {
    val pluralStem = plural.withoutDash().lowercase().replace(pluralSuffixRegex, "")
    if (pluralStem.length < MIN_PLURAL_STEM_LENGTH) return false

    return pluralStem.vowels() !in baseWord.lowercase().vowels()
}
