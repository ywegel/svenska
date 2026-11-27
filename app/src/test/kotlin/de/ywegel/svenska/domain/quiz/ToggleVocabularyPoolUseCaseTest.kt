package de.ywegel.svenska.domain.quiz

import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.data.model.quiz.QuizFacet
import de.ywegel.svenska.data.model.quiz.VocabularyPoolState
import de.ywegel.svenska.data.model.vocabulary
import de.ywegel.svenska.fakes.QuizPoolRepositoryFake
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty
import kotlin.time.Instant

class ToggleVocabularyPoolUseCaseTest {
    private val pool = QuizPoolRepositoryFake()
    private val toggle = ToggleVocabularyPoolUseCase(QueueToPoolUseCase(pool), pool)

    @Test
    fun `A word that is not in the pool is queued`() = runTest {
        // When
        toggle(WORD, VocabularyPoolState.NotInPool, NOW)

        // Then
        expectThat(pool.queuedToPool).containsExactly(WORD)
    }

    @Test
    fun `A paused word is resumed`() = runTest {
        // When
        toggle(WORD, VocabularyPoolState.Paused, NOW)

        // Then
        expectThat(pool.resumedInPool).containsExactly(WORD.id)
        expectThat(pool.queuedToPool).isEmpty()
    }

    @ParameterizedTest
    @MethodSource("wordsInPool")
    fun `A word in the pool is removed`(poolState: VocabularyPoolState) = runTest {
        // When
        toggle(WORD, poolState, NOW)

        // Then
        expectThat(pool.removedFromPool).containsExactly(WORD.id)
    }

    private companion object {
        val NOW = Instant.fromEpochMilliseconds(1_000L)
        val WORD = vocabulary(id = 7, word = "tack", wordGroup = WordGroup.Other).copy(ending = "")

        @JvmStatic
        fun wordsInPool(): List<Arguments> = listOf(
            Arguments.of(VocabularyPoolState.Queued),
            Arguments.of(VocabularyPoolState.Active(setOf(QuizFacet.SV_DE))),
        )
    }
}
