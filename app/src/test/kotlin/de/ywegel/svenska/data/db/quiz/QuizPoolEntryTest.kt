package de.ywegel.svenska.data.db.quiz

import de.ywegel.svenska.data.model.quiz.QuizPoolStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo

class QuizPoolEntryTest {
    @ParameterizedTest
    @MethodSource("statusCases")
    fun `The pool status is derived from the fields`(entry: QuizPoolEntry, expected: QuizPoolStatus) {
        // When
        val status = entry.poolStatus

        // Then
        expectThat(status).isEqualTo(expected)
    }

    @Test
    fun `A queued word can not be paused`() {
        // Given
        val entry = queued()

        // When / Then
        expectThrows<IllegalArgumentException> { entry.pause() }
    }

    @Test
    fun `A queued word can not have introduced its forms`() {
        // When / Then
        expectThrows<IllegalArgumentException> { queued().copy(formsIntroducedAt = LATER) }
    }

    @Test
    fun `Introducing a queued word makes it active at that time`() {
        // Given
        val entry = queued()

        // When
        val introduced = entry.introduce(now = INTRODUCED_AT)

        // Then
        expectThat(introduced).isEqualTo(entry.copy(introducedAt = INTRODUCED_AT))
    }

    @ParameterizedTest
    @MethodSource("introducedCases")
    fun `An introduced word can not be introduced again`(entry: QuizPoolEntry) {
        // When / Then
        expectThrows<IllegalStateException> { entry.introduce(now = LATER) }
    }

    @Test
    fun `Pausing an active word keeps its progress`() {
        // Given
        val entry = active(svDeStreak = 2)

        // When
        val paused = entry.pause()

        // Then
        expectThat(paused).isEqualTo(entry.copy(isPaused = true))
    }

    @Test
    fun `Resuming a paused word makes it active without introducing it again`() {
        // Given
        val entry = paused()

        // When
        val resumed = entry.resume()

        // Then
        expectThat(resumed).isEqualTo(active())
    }

    @Test
    fun `Resetting a word clears its streak and keeps its introduction`() {
        // Given
        val entry = active(svDeStreak = 5)

        // When
        val reset = entry.reset(now = LATER)

        // Then
        expectThat(reset).isEqualTo(active(svDeStreak = 0).copy(resetAt = LATER))
    }

    companion object {
        private const val VOCABULARY_ID = 1
        private const val QUEUED_AT = 1_000L
        private const val INTRODUCED_AT = 2_000L
        private const val LATER = 3_000L

        private fun queued() = QuizPoolEntry(vocabularyId = VOCABULARY_ID, queuedAt = QUEUED_AT)

        private fun active(svDeStreak: Int = 0) = queued().copy(introducedAt = INTRODUCED_AT, svDeStreak = svDeStreak)

        private fun paused() = active().copy(isPaused = true)

        @JvmStatic
        fun statusCases(): List<Arguments> = listOf(
            argumentSet("not introduced is queued", queued(), QuizPoolStatus.QUEUED),
            argumentSet("introduced is active", active(), QuizPoolStatus.ACTIVE),
            argumentSet("introduced and paused is paused", paused(), QuizPoolStatus.PAUSED),
        )

        @JvmStatic
        fun introducedCases(): List<Arguments> = listOf(
            argumentSet("active", active()),
            argumentSet("paused", paused()),
        )
    }
}
