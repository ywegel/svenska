package de.ywegel.svenska.data.preferences

/**
 * Returns a copy with [item] moved to the front, deduplicated, capped at [limit] entries.
 */
fun <T> ArrayDeque<T>.addedToFrontAndLimited(item: T, limit: Int = LAST_SEARCHED_LIMIT): ArrayDeque<T> =
    ArrayDeque(this).apply {
        remove(item)
        addFirst(item)
        while (size > limit) {
            removeLast()
        }
    }

private const val LAST_SEARCHED_LIMIT = 8
