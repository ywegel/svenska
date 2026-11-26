package de.ywegel.svenska.domain.quiz

import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.data.model.vocabulary
import de.ywegel.svenska.fakes.QuizPoolRepositoryFake
import de.ywegel.svenska.testdata.ReferenceWords
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.containsExactly
import kotlin.time.Instant

class QueueToPoolUseCaseTest {
    @ParameterizedTest
    @MethodSource("wordsWithBuiltForms")
    fun `Queueing builds the forms of a word without forms`(expected: Vocabulary) = runTest {
        // Given
        val pool = QuizPoolRepositoryFake()

        // When
        QueueToPoolUseCase(pool)(listOf(expected.withoutForms()), NOW)

        // Then
        expectThat(pool.queuedToPool).containsExactly(expected)
    }

    @Test
    fun `Queueing flags built forms that need review`() = runTest {
        // Given
        val skafferi = ReferenceWords.byWord("skafferi").toVocabulary().withoutForms()
        val pool = QuizPoolRepositoryFake()

        // When
        QueueToPoolUseCase(pool)(listOf(skafferi), NOW)

        // Then
        expectThat(pool.queuedToPool.map { it.formsNeedReview }).containsExactly(true)
    }

    @Test
    fun `Queueing keeps the forms a word already has`() = runTest {
        // Given: the user corrected the plural, which the endings would build differently
        val corrected = ReferenceWords.byWord("bil").toVocabulary().copy(form2 = "bilor", formsNeedReview = true)
        val pool = QuizPoolRepositoryFake()

        // When
        QueueToPoolUseCase(pool)(listOf(corrected), NOW)

        // Then
        expectThat(pool.queuedToPool).containsExactly(corrected)
    }

    @Test
    fun `Queueing builds no forms for Other`() = runTest {
        // Given
        val phrase = vocabulary(word = "tack så mycket", wordGroup = WordGroup.Other).copy(ending = "")
        val pool = QuizPoolRepositoryFake()

        // When
        QueueToPoolUseCase(pool)(listOf(phrase), NOW)

        // Then
        expectThat(pool.queuedToPool).containsExactly(phrase)
    }

    private companion object {
        val NOW = Instant.fromEpochMilliseconds(1_000L)

        fun Vocabulary.withoutForms() = copy(form1 = null, form2 = null, form3 = null, formsNeedReview = false)

        @JvmStatic
        fun wordsWithBuiltForms(): List<Arguments> = listOf(
            argumentSet("noun", ReferenceWords.byWord("bil").toVocabulary()),
            argumentSet("verb", ReferenceWords.byWord("svara").toVocabulary()),
            argumentSet("adjective with two forms", ReferenceWords.byWord("glad").toVocabulary()),
        )
    }
}
