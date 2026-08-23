package de.ywegel.svenska.data.preferences

/**
 * Returns a copy with [item] moved to the front, deduplicated, capped at [limit] entries.
 */
fun <T> List<T>.addedToFrontAndLimited(item: T, limit: Int = LAST_SEARCHED_LIMIT): List<T> =
    (listOf(item) + this.filterNot { it == item }).take(limit)

private const val LAST_SEARCHED_LIMIT = 8
