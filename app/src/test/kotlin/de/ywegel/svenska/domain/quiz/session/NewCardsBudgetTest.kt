package de.ywegel.svenska.domain.quiz.session

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class NewCardsBudgetTest {
    @ParameterizedTest
    @MethodSource("budgets")
    fun `New words fill the daily limit, unless the due reviews exceed the backlog limit`(
        introducedToday: Int,
        dueCount: Int,
        expected: Int,
    ) {
        // When
        val allowed = newWordsAllowedToday(introducedToday, dueCount, NEW_PER_DAY, BACKLOG_LIMIT)

        // Then
        expectThat(allowed).isEqualTo(expected)
    }

    companion object {
        private const val NEW_PER_DAY = 10
        private const val BACKLOG_LIMIT = 100

        @JvmStatic
        fun budgets() = listOf(
            argumentSet("nothing introduced yet", 0, 0, 10),
            argumentSet("part of the limit used", 4, 0, 6),
            argumentSet("limit lowered below what was introduced", 12, 0, 0),
            argumentSet("due reviews at the backlog limit", 0, 100, 10),
            argumentSet("due reviews above the backlog limit", 0, 101, 0),
        )
    }
}
