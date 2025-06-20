package de.ywegel.svenska.data.preferences

fun <T> ArrayDeque<T>.addToFrontAndLimit(item: T, limit: Int = LAST_SEARCHED_LIMIT) {
    this.remove(item)
    this.addFirst(item)
    while (this.size > limit) {
        this.removeLast()
    }
}

private const val LAST_SEARCHED_LIMIT = 8
