package de.ywegel.svenska.domain.words

private const val MIN_OVERLAY_LENGTH = 3
private const val MIN_LONG_PIECE_OVERLAY_LENGTH = 2
private const val LONG_PIECE_LENGTH = 4

/**
 * Lays a shortened compound over the end of the word, where the longest beginning of [piece] (at least
 * [minLength] letters) appears: hurtbulle + bullar -> hurtbullar, trafikolycka + olyckor -> trafikolyckor.
 * Returns null if nothing matches.
 */
fun overlay(word: String, piece: String, minLength: Int = MIN_OVERLAY_LENGTH): String? {
    val start = (piece.length downTo minLength)
        .map { piece.take(it) }
        .firstOrNull { it in word }
        ?: return null
    return word.substringBeforeLast(start) + piece
}

/**
 * Overlay for pieces written with a dash. A vowel change leaves only two letters to match:
 * storstad + städer -> storstäder, bidra + drog -> bidrog.
 * Short pieces would match by accident: kortfattad + at -> kortfat.
 */
fun overlayAbbreviated(word: String, piece: String): String? {
    val minLength = if (piece.length >= LONG_PIECE_LENGTH) MIN_LONG_PIECE_OVERLAY_LENGTH else MIN_OVERLAY_LENGTH
    return overlay(word, piece, minLength)
}
