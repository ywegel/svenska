package de.ywegel.svenska.domain.quiz.session

import de.ywegel.svenska.data.model.vocabulary
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNull
import strikt.assertions.isTrue

class QuizSessionTest {
    @Test
    fun `A repeated item comes again after four other items`() {
        // Given
        val session = QuizSession(items(1..6))

        // When
        session.answer(repeat = FORMS_OF_1)

        // Then
        expectThat(session.remaining()).containsExactly(item(2), item(3), item(4), item(5), FORMS_OF_1, item(6))
    }

    @Test
    fun `A repeated item near the end comes last`() {
        // Given
        val session = QuizSession(items(1..2))

        // When
        session.answer(repeat = FORMS_OF_1)

        // Then
        expectThat(session.remaining()).containsExactly(item(2), FORMS_OF_1)
    }

    @ParameterizedTest
    @MethodSource("rounds")
    fun `The session asks to continue after every 25 items, unless it is done`(
        itemCount: Int,
        answered: Int,
        expected: Boolean,
    ) {
        // Given
        val session = QuizSession(items(1..itemCount))

        // When
        repeat(answered) { session.answer() }

        // Then
        expectThat(session.isEndOfRound).isEqualTo(expected)
    }

    @Test
    fun `After continuing, the next round ends after another 25 items`() {
        // Given
        val session = QuizSession(items(1..60))
        repeat(25) { session.answer() }

        // When
        session.continueRound()

        // Then
        expectThat(session.isEndOfRound).isFalse()
        repeat(25) { session.answer() }
        expectThat(session.isEndOfRound).isTrue()
    }

    @Test
    fun `Answering a done session does nothing`() {
        // Given
        val session = QuizSession(items(1..1))
        session.answer()

        // When
        session.answer()

        // Then
        expectThat(session.current).isNull()
    }

    @Test
    fun `Only an item of the answered word can be repeated`() {
        // Given
        val session = QuizSession(items(2..3))

        // When / Then
        expectThrows<IllegalArgumentException> { session.answer(repeat = FORMS_OF_1) }
    }

    private fun QuizSession.remaining(): List<QuizItem> = buildList {
        while (current != null) {
            add(checkNotNull(current))
            answer()
        }
    }

    companion object {
        private val FORMS_OF_1 = QuizItem.Forms(vocabulary(id = 1))

        // Built once, as vocabulary() stamps the current time
        private val ITEMS = (1..60).map { QuizItem.SvDe(vocabulary(id = it)) }

        private fun item(id: Int) = ITEMS[id - 1]

        private fun items(ids: IntRange) = ids.map(::item)

        @JvmStatic
        fun rounds() = listOf(
            Arguments.of(30, 24, false),
            Arguments.of(30, 25, true),
            Arguments.of(25, 25, false),
        )
    }
}
